package com.example.refactored_student_housing.service;

import com.example.refactored_student_housing.converter.StudentApiMapper;
import com.example.refactored_student_housing.domain.AppUser;
import com.example.refactored_student_housing.domain.Student;
import com.example.refactored_student_housing.dto.*;
import com.example.refactored_student_housing.exception.ResourceNotFoundException;
import com.example.refactored_student_housing.repository.AppUserRepository;
import com.example.refactored_student_housing.repository.SearchPatterns;
import com.example.refactored_student_housing.repository.StudentRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * The transactional boundary of the application sits here, on the service
 * layer: a controller never opens a transaction and never sees an entity.
 */
@Service
@Transactional
public class StudentServiceImpl implements StudentService {

    /** Age assumed for API-created students, which do not submit a birth date. */
    private static final int DEFAULT_API_AGE = 18;
    private static final String SEARCH_CACHE = "students-search";

    private final StudentRepository studentRepository;
    private final AppUserRepository userRepository;
    private final LeaseService leaseService;
    private final StudentApiMapper mapper;

    public StudentServiceImpl(StudentRepository studentRepository,
                              AppUserRepository userRepository,
                              LeaseService leaseService,
                              StudentApiMapper mapper) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.leaseService = leaseService;
        this.mapper = mapper;
    }

    // ------------------------------------------------------------------
    // Reading
    // ------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<StudentListItemDto> findStudents(StudentFilter filter, UserDetails currentUser) {
        boolean admin = isAdmin(currentUser);
        String username = usernameOf(currentUser);

        // Week 5: a signed-in, non-admin advisor only manages their own records.
        // This restriction is pushed into the WHERE clause instead of loading
        // every student and dropping rows in Java afterwards.
        StudentFilter effective = (admin || username == null) ? filter : filter.restrictedTo(username);

        return search(effective).stream()
                .map(student -> toListItem(student, admin, username))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDetailDto findStudentDetail(Long id, UserDetails currentUser) {
        Student student = studentRepository.findDetailById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Student", id));

        boolean mayManage = mayManage(student, currentUser);

        List<LeaseSummaryDto> leases = student.getLeases().stream()
                .map(lease -> new LeaseSummaryDto(
                        lease.getHousingUnit() == null ? null : lease.getHousingUnit().getId(),
                        lease.getHousingUnit() == null ? null : lease.getHousingUnit().getAddress(),
                        lease.getStartDate(),
                        lease.getEndDate(),
                        lease.isActive()))
                .toList();

        return new StudentDetailDto(
                student.getId(), student.getName(), student.getBirthDate(), student.getYearOfStudy(),
                mayManage ? student.getGpa() : null, mayManage, student.isInternational(),
                student.getProfileImageUrl(), mayManage, leases);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDto findStudentForApi(Long id, UserDetails currentUser) {
        Student student = studentRepository.findDetailById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Student", id));

        StudentDto dto = mapper.toDto(student);
        if (!mayManage(student, currentUser)) {
            dto.setGpa(null);
        }
        return dto;
    }

    /**
     * Week 12: the same search term never reaches the database twice. Immutable
     * DTOs are cached rather than entities - caching managed entities would hand
     * detached objects to a later transaction.
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(SEARCH_CACHE)
    public List<StudentSearchDto> searchByName(String name) {
        return mapper.toSearchDtos(search(StudentFilter.of(name, null, null)));
    }

    // ------------------------------------------------------------------
    // Writing
    // ------------------------------------------------------------------

    @Override
    @CacheEvict(value = SEARCH_CACHE, allEntries = true)
    public Long createFromForm(StudentFormDto form, String advisorUsername) {
        Student student = new Student();
        student.setName(form.getName());
        student.setBirthDate(form.getBirthDate());
        student.setYearOfStudy(form.getYearOfStudy());
        student.setGpa(form.getGpa());
        student.setInternational(form.isInternational());
        student.setProfileImageUrl(form.getProfileImageUrl());
        // Week 5: the creator becomes the owner of the record.
        student.setAdvisor(findAdvisor(advisorUsername));

        Student saved = studentRepository.save(student);

        if (form.getLinkUnitId() != null) {
            leaseService.createStandardLease(saved.getId(), form.getLinkUnitId());
        }
        return saved.getId();
    }

    @Override
    @CacheEvict(value = SEARCH_CACHE, allEntries = true)
    public StudentDto createFromApi(StudentCreateDto dto, String advisorUsername) {
        Student student = mapper.toEntity(dto);
        // The API only accepts a name and a year of study; the remaining
        // properties get their defaults here, in the business layer.
        student.setGpa(0.0);
        student.setInternational(false);
        student.setBirthDate(LocalDate.now().minusYears(DEFAULT_API_AGE));
        student.setAdvisor(findAdvisor(advisorUsername));

        return mapper.toDto(studentRepository.save(student));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @studentSecurity.mayModify(#id, authentication)")
    @CacheEvict(value = SEARCH_CACHE, allEntries = true)
    public StudentDto updateGpa(Long id, StudentUpdateDto dto) {
        Student student = studentRepository.findDetailById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Student", id));

        mapper.updateEntityFromDto(dto, student);
        return mapper.toDto(studentRepository.save(student));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @studentSecurity.mayModify(#id, authentication)")
    @CacheEvict(value = SEARCH_CACHE, allEntries = true)
    public void deleteById(Long id) {
        if (!studentRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Student", id);
        }
        studentRepository.deleteById(id);
    }

    @Override
    @CacheEvict(value = SEARCH_CACHE, allEntries = true)
    public int importStudents(List<StudentImportRow> rows) {
        List<Student> students = new ArrayList<>(rows.size());
        for (StudentImportRow row : rows) {
            Student student = new Student();
            student.setName(row.name());
            student.setBirthDate(row.birthDate());
            student.setYearOfStudy(row.yearOfStudy());
            student.setGpa(row.gpa());
            student.setInternational(row.international());
            students.add(student);
        }
        return studentRepository.saveAll(students).size();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private List<Student> search(StudentFilter filter) {
        return studentRepository.search(SearchPatterns.contains(filter.nameContains()),
                filter.minYear(), filter.bornBefore(), filter.minGpa(),
                filter.internationalOnly(), filter.restrictToAdvisor());
    }

    private StudentListItemDto toListItem(Student student, boolean admin, String username) {
        boolean mayManage = admin || isOwnedBy(student, username);
        return new StudentListItemDto(
                student.getId(), student.getName(), student.getYearOfStudy(),
                mayManage ? student.getGpa() : null, mayManage,
                student.isInternational(), student.getProfileImageUrl(), mayManage);
    }

    private boolean mayManage(Student student, UserDetails currentUser) {
        return isAdmin(currentUser) || isOwnedBy(student, usernameOf(currentUser));
    }

    private boolean isOwnedBy(Student student, String username) {
        return username != null
                && student.getAdvisor() != null
                && username.equals(student.getAdvisor().getUsername());
    }

    private AppUser findAdvisor(String username) {
        return username == null ? null : userRepository.findByUsername(username).orElse(null);
    }

    private static boolean isAdmin(UserDetails user) {
        return user != null && user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private static String usernameOf(UserDetails user) {
        return user == null ? null : user.getUsername();
    }
}
