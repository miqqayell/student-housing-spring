package com.example.refactored_student_housing.service;

import com.example.refactored_student_housing.domain.AppUser;
import com.example.refactored_student_housing.domain.Student;
import com.example.refactored_student_housing.dto.StudentUpdateDto;
import com.example.refactored_student_housing.exception.ResourceNotFoundException;
import com.example.refactored_student_housing.repository.AppUserRepository;
import com.example.refactored_student_housing.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Week 8 - role / authorization verification tests.
 *
 * Verifies the requirement "updating and deleting records is only allowed by
 * the individual user associated with that specific record and the
 * administrator". The rule is implemented with {@code @PreAuthorize} on
 * {@link StudentService}, so it is tested here, directly on the annotated
 * service methods.
 *
 * Each secured method is verified for: the owner, an administrator, a different
 * authenticated user, an anonymous caller, and the not-found edge case.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StudentServiceAuthorizationIT {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AppUserRepository userRepository;

    private Long studentId;

    @BeforeEach
    void setUp() {
        AppUser owner = new AppUser();
        owner.setUsername("owner");
        owner.setPassword("pw");
        owner.setEmail("owner@test.com");
        owner.setAge(30);
        owner.setRole("USER");
        userRepository.saveAndFlush(owner);

        Student student = new Student();
        student.setName("Owned Student");
        student.setYearOfStudy(2);
        student.setBirthDate(LocalDate.of(2002, 3, 1));
        student.setInternational(false);
        student.setGpa(3.0);
        student.setAdvisor(owner);
        studentId = studentRepository.saveAndFlush(student).getId();
    }

    private static StudentUpdateDto newGpa() {
        StudentUpdateDto dto = new StudentUpdateDto();
        dto.setGpa(3.9);
        return dto;
    }

    // ---------- updateGpa ----------

    @Test
    @WithMockUser(username = "owner", roles = "USER")
    void updateGpa_asOwner_isAllowed() {
        assertEquals(3.9, studentService.updateGpa(studentId, newGpa()).getGpa());
    }

    @Test
    @WithMockUser(username = "someAdmin", roles = "ADMIN")
    void updateGpa_asAdmin_isAllowed() {
        assertEquals(3.9, studentService.updateGpa(studentId, newGpa()).getGpa());
    }

    @Test
    @WithMockUser(username = "intruder", roles = "USER")
    void updateGpa_asAnotherUser_isDenied() {
        assertThrows(AccessDeniedException.class, () -> studentService.updateGpa(studentId, newGpa()));

        assertEquals(3.0, studentRepository.findById(studentId).orElseThrow().getGpa(),
                "The GPA must be unchanged after a denied update");
    }

    @Test
    @WithAnonymousUser
    void updateGpa_asAnonymous_isDenied() {
        assertThrows(AccessDeniedException.class, () -> studentService.updateGpa(studentId, newGpa()));
    }

    @Test
    @WithMockUser(username = "owner", roles = "USER")
    void updateGpa_whenMissing_reportsNotFoundRatherThanForbidden() {
        assertThrows(ResourceNotFoundException.class, () -> studentService.updateGpa(999999L, newGpa()));
    }

    // ---------- deleteById ----------

    @Test
    @WithMockUser(username = "owner", roles = "USER")
    void delete_asOwner_isAllowed() {
        studentService.deleteById(studentId);

        assertFalse(studentRepository.findById(studentId).isPresent());
    }

    @Test
    @WithMockUser(username = "someAdmin", roles = "ADMIN")
    void delete_asAdmin_isAllowed() {
        studentService.deleteById(studentId);

        assertFalse(studentRepository.findById(studentId).isPresent());
    }

    @Test
    @WithMockUser(username = "intruder", roles = "USER")
    void delete_asAnotherUser_isDenied() {
        assertThrows(AccessDeniedException.class, () -> studentService.deleteById(studentId));

        assertTrue(studentRepository.findById(studentId).isPresent(),
                "The student must still exist after a denied delete");
    }

    @Test
    @WithAnonymousUser
    void delete_asAnonymous_isDenied() {
        assertThrows(AccessDeniedException.class, () -> studentService.deleteById(studentId));
    }
}
