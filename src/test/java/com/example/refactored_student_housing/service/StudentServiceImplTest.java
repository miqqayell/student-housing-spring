package com.example.refactored_student_housing.service;

import com.example.refactored_student_housing.converter.StudentApiMapper;
import com.example.refactored_student_housing.domain.AppUser;
import com.example.refactored_student_housing.domain.Student;
import com.example.refactored_student_housing.dto.StudentCreateDto;
import com.example.refactored_student_housing.dto.StudentDto;
import com.example.refactored_student_housing.dto.StudentFilter;
import com.example.refactored_student_housing.dto.StudentFormDto;
import com.example.refactored_student_housing.exception.ResourceNotFoundException;
import com.example.refactored_student_housing.repository.AppUserRepository;
import com.example.refactored_student_housing.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Week 9 - pure unit tests (Mockito, no Spring context) for the business layer.
 *
 * Every collaborator of the service is mocked, so only the service's own logic
 * runs. Three methods are covered with several tests each, and {@code verify}
 * asserts the collaborators are called with exactly the right arguments - most
 * importantly that the role restriction is pushed into the repository query
 * instead of being applied afterwards in memory.
 */
@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private LeaseService leaseService;

    @Mock
    private StudentApiMapper mapper;

    @InjectMocks
    private StudentServiceImpl service;

    @Captor
    private ArgumentCaptor<Student> studentCaptor;

    private static final UserDetails ADMIN = User.withUsername("boss").password("x").roles("ADMIN").build();
    private static final UserDetails ADVISOR = User.withUsername("ann").password("x").roles("USER").build();

    // ---------- findStudents: the role restriction must reach the database ----------

    @Test
    void findStudents_asAdvisor_restrictsTheQueryToTheirOwnRecords() {
        when(studentRepository.search(eq("%"), isNull(), isNull(), isNull(), eq(false), eq("ann")))
                .thenReturn(List.of());

        service.findStudents(StudentFilter.of(null, null, null), ADVISOR);

        // The advisor's username is passed to the query itself - no post-filtering.
        verify(studentRepository).search("%", null, null, null, false, "ann");
    }

    @Test
    void findStudents_asAdmin_doesNotRestrictTheQuery() {
        when(studentRepository.search(eq("%"), isNull(), isNull(), isNull(), eq(false), isNull()))
                .thenReturn(List.of());

        service.findStudents(StudentFilter.of(null, null, null), ADMIN);

        verify(studentRepository).search("%", null, null, null, false, null);
    }

    @Test
    void findStudents_hidesTheGpaFromUsersWhoDoNotOwnTheRecord() {
        AppUser otherAdvisor = new AppUser();
        otherAdvisor.setUsername("someoneElse");

        Student student = new Student();
        student.setId(1L);
        student.setName("Foreign Student");
        student.setGpa(3.9);
        student.setAdvisor(otherAdvisor);

        when(studentRepository.search(eq("%"), isNull(), isNull(), isNull(), eq(false), eq("ann")))
                .thenReturn(List.of(student));

        var result = service.findStudents(StudentFilter.of(null, null, null), ADVISOR);

        assertEquals(1, result.size());
        assertNull(result.getFirst().getGpa(), "The GPA must not leak to a non-owner");
        assertTrue(!result.getFirst().isGpaVisible());
    }

    // ---------- deleteById ----------

    @Test
    void deleteById_whenStudentExists_deletesThatExactId() {
        when(studentRepository.existsById(7L)).thenReturn(true);

        service.deleteById(7L);

        verify(studentRepository).deleteById(7L);
    }

    @Test
    void deleteById_whenStudentMissing_throwsAndNeverDeletes() {
        when(studentRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.deleteById(99L));

        verify(studentRepository, never()).deleteById(anyLong());
    }

    // ---------- createFromApi: the defaults belong to the business layer ----------

    @Test
    void createFromApi_appliesDefaultsAndLinksTheCreator() {
        AppUser creator = new AppUser();
        creator.setUsername("ann");

        StudentCreateDto dto = new StudentCreateDto();
        dto.setName("Eve Developer");
        dto.setYearOfStudy(1);

        Student mapped = new Student();
        mapped.setName("Eve Developer");
        mapped.setYearOfStudy(1);

        when(mapper.toEntity(dto)).thenReturn(mapped);
        when(userRepository.findByUsername("ann")).thenReturn(Optional.of(creator));
        when(studentRepository.save(any(Student.class))).thenAnswer(call -> call.getArgument(0));
        when(mapper.toDto(any(Student.class))).thenReturn(new StudentDto());

        service.createFromApi(dto, "ann");

        verify(studentRepository).save(studentCaptor.capture());
        Student saved = studentCaptor.getValue();
        assertEquals(0.0, saved.getGpa());
        assertEquals(LocalDate.now().minusYears(18), saved.getBirthDate());
        assertTrue(!saved.isInternational());
        assertEquals("ann", saved.getAdvisor().getUsername(), "The creator must own the new record");
    }

    // ---------- createFromForm: the optional lease is delegated, not inlined ----------

    @Test
    void createFromForm_withoutAUnit_neverCreatesALease() {
        StudentFormDto form = new StudentFormDto();
        form.setName("No Housing");
        form.setYearOfStudy(2);

        when(studentRepository.save(any(Student.class))).thenAnswer(call -> {
            Student student = call.getArgument(0);
            student.setId(5L);
            return student;
        });

        service.createFromForm(form, null);

        verify(leaseService, never()).createStandardLease(anyLong(), anyLong());
    }

    @Test
    void createFromForm_withAUnit_delegatesTheLeaseToTheLeaseService() {
        StudentFormDto form = new StudentFormDto();
        form.setName("With Housing");
        form.setYearOfStudy(2);
        form.setLinkUnitId(42L);

        when(studentRepository.save(any(Student.class))).thenAnswer(call -> {
            Student student = call.getArgument(0);
            student.setId(5L);
            return student;
        });

        service.createFromForm(form, null);

        // The leasing rules live in the lease service, not in this service.
        verify(leaseService).createStandardLease(5L, 42L);
    }
}
