package com.example.refactored_student_housing.web.api;

import com.example.refactored_student_housing.domain.AppUser;
import com.example.refactored_student_housing.domain.Student;
import com.example.refactored_student_housing.repository.AppUserRepository;
import com.example.refactored_student_housing.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Week 8 - Role / authorization verification tests.
 *
 * Verifies the security requirement: "updating and deleting records is only
 * allowed by the individual user associated with that specific record and the
 * administrator." Authorization is enforced in {@link StudentApiController} on
 * methods that take an {@code @AuthenticationPrincipal} parameter (PATCH/DELETE).
 *
 * Each secured method is verified for: success (owner), success (admin),
 * failure (a different authenticated user), and the not-found edge case.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StudentApiAuthorizationIT {

    @Autowired
    private MockMvc mockMvc;

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
        studentId = studentRepository.save(student).getId();
    }

    private static final String PATCH_BODY = "{\"gpa\":3.9}";

    // ---------- PATCH (update) authorization ----------

    @Test
    void updateGpa_asOwner_succeeds() throws Exception {
        mockMvc.perform(patch("/api/students/{id}", studentId)
                        .with(user("owner").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PATCH_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gpa").value(3.9));
    }

    @Test
    void updateGpa_asAdmin_succeeds() throws Exception {
        mockMvc.perform(patch("/api/students/{id}", studentId)
                        .with(user("someAdmin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PATCH_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gpa").value(3.9));
    }

    @Test
    void updateGpa_asOtherUser_isForbidden() throws Exception {
        mockMvc.perform(patch("/api/students/{id}", studentId)
                        .with(user("intruder").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PATCH_BODY))
                .andExpect(status().isForbidden());

        assertTrue(studentRepository.findById(studentId).filter(s -> s.getGpa() == 3.0).isPresent(),
                "GPA must be unchanged after a forbidden update");
    }

    @Test
    void updateGpa_whenMissing_returns404() throws Exception {
        mockMvc.perform(patch("/api/students/{id}", 999999L)
                        .with(user("owner").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PATCH_BODY))
                .andExpect(status().isNotFound());
    }

    // ---------- DELETE authorization ----------

    @Test
    void deleteStudent_asOwner_succeeds() throws Exception {
        mockMvc.perform(delete("/api/students/{id}", studentId)
                        .with(user("owner").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertFalse(studentRepository.findById(studentId).isPresent(),
                "Student should be deleted by its owner");
    }

    @Test
    void deleteStudent_asAdmin_succeeds() throws Exception {
        mockMvc.perform(delete("/api/students/{id}", studentId)
                        .with(user("someAdmin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertFalse(studentRepository.findById(studentId).isPresent(),
                "Student should be deletable by an admin");
    }

    @Test
    void deleteStudent_asOtherUser_isForbidden() throws Exception {
        mockMvc.perform(delete("/api/students/{id}", studentId)
                        .with(user("intruder").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertTrue(studentRepository.findById(studentId).isPresent(),
                "Student must still exist after a forbidden delete");
    }

    @Test
    void deleteStudent_whenMissing_returns404() throws Exception {
        mockMvc.perform(delete("/api/students/{id}", 999999L)
                        .with(user("owner").roles("USER"))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
