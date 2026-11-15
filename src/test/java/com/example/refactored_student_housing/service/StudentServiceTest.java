package com.example.refactored_student_housing.service;

import com.example.refactored_student_housing.domain.AppUser;
import com.example.refactored_student_housing.domain.Student;
import com.example.refactored_student_housing.dto.StudentFilter;
import com.example.refactored_student_housing.dto.StudentFormDto;
import com.example.refactored_student_housing.dto.StudentListItemDto;
import com.example.refactored_student_housing.exception.ResourceNotFoundException;
import com.example.refactored_student_housing.repository.AppUserRepository;
import com.example.refactored_student_housing.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Week 6 - integration tests of the business layer (service + repository).
 *
 * Two service methods are covered with a success and a failure path each:
 * the role-aware listing and the delete.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class StudentServiceTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AppUserRepository userRepository;

    private static final UserDetails OWNER = User.withUsername("serviceOwner").password("x").roles("USER").build();
    private static final UserDetails ADMIN = User.withUsername("serviceAdmin").password("x").roles("ADMIN").build();

    private Long ownedStudentId;

    @BeforeEach
    void setUp() {
        AppUser owner = new AppUser();
        owner.setUsername("serviceOwner");
        owner.setPassword("pw");
        owner.setEmail("serviceowner@test.com");
        owner.setAge(40);
        owner.setRole("USER");
        userRepository.saveAndFlush(owner);

        Student owned = new Student();
        owned.setName("Xavier Unique");
        owned.setYearOfStudy(3);
        owned.setBirthDate(LocalDate.of(2000, 1, 1));
        owned.setInternational(false);
        owned.setGpa(3.5);
        owned.setAdvisor(owner);
        ownedStudentId = studentRepository.saveAndFlush(owned).getId();

        Student foreign = new Student();
        foreign.setName("Yolanda Other");
        foreign.setYearOfStudy(1);
        foreign.setBirthDate(LocalDate.of(2003, 6, 6));
        foreign.setInternational(true);
        foreign.setGpa(2.5);
        studentRepository.saveAndFlush(foreign);
    }

    // ---------- findStudents ----------

    @Test
    void findStudents_byName_returnsTheMatchingStudent() {
        List<StudentListItemDto> results =
                studentService.findStudents(StudentFilter.of("Xavier", null, null), ADMIN);

        assertEquals(1, results.size());
        assertEquals("Xavier Unique", results.getFirst().getName());
    }

    @Test
    void findStudents_withoutMatches_returnsEmptyList() {
        assertTrue(studentService.findStudents(StudentFilter.of("Zebra", null, null), ADMIN).isEmpty());
    }

    @Test
    void findStudents_asAdvisor_onlyReturnsTheirOwnRecords() {
        List<StudentListItemDto> results =
                studentService.findStudents(StudentFilter.of(null, null, null), OWNER);

        assertEquals(1, results.size());
        assertEquals("Xavier Unique", results.getFirst().getName());
        assertTrue(results.getFirst().isGpaVisible(), "An advisor may see the GPA of their own student");
    }

    @Test
    void findStudents_asAnonymous_hidesEveryGpa() {
        List<StudentListItemDto> results =
                studentService.findStudents(StudentFilter.of(null, null, null), null);

        assertEquals(2, results.size(), "Anonymous visitors still see the full list");
        assertTrue(results.stream().noneMatch(StudentListItemDto::isGpaVisible));
        assertTrue(results.stream().allMatch(dto -> dto.getGpa() == null));
    }

    // ---------- deleteById ----------

    @Test
    @WithMockUser(username = "serviceOwner", roles = "USER")
    void deleteById_asOwner_removesTheStudent() {
        studentService.deleteById(ownedStudentId);

        assertTrue(studentRepository.findById(ownedStudentId).isEmpty());
    }

    @Test
    @WithMockUser(username = "serviceOwner", roles = "USER")
    void deleteById_whenMissing_throwsResourceNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> studentService.deleteById(9999L));
    }

    // ---------- createFromForm ----------

    @Test
    void createFromForm_linksTheCreatorAsAdvisor() {
        StudentFormDto form = new StudentFormDto();
        form.setName("Newly Created");
        form.setBirthDate(LocalDate.of(2004, 2, 2));
        form.setYearOfStudy(1);
        form.setGpa(3.1);

        Long id = studentService.createFromForm(form, "serviceOwner");

        Student created = studentRepository.findWithAdvisorById(id).orElseThrow();
        assertNotNull(created.getAdvisor(), "The creator must become the owner of the record");
        assertEquals("serviceOwner", created.getAdvisor().getUsername());
    }
}
