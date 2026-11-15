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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Week 8 - API integration tests for the REST presentation layer.
 * Covers GET /api/students/{id} (role-dependent GPA visibility, 404),
 * GET /api/students?name=... (the search used by the Client project) and
 * POST /api/students (201 Created with a Location header).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StudentApiControllerIT {

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
        student.setName("Api Test Student");
        student.setYearOfStudy(2);
        student.setBirthDate(LocalDate.of(2002, 3, 1));
        student.setInternational(false);
        student.setGpa(3.7);
        student.setAdvisor(owner);
        studentId = studentRepository.saveAndFlush(student).getId();
    }

    @Test
    void getStudent_asAdmin_returnsGpa() throws Exception {
        mockMvc.perform(get("/api/students/{id}", studentId)
                        .with(user("adminUser").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Api Test Student"))
                .andExpect(jsonPath("$.gpa").value(3.7));
    }

    @Test
    void getStudent_asOwner_returnsGpa() throws Exception {
        mockMvc.perform(get("/api/students/{id}", studentId)
                        .with(user("owner").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gpa").value(3.7));
    }

    @Test
    void getStudent_asAnonymous_hidesGpa() throws Exception {
        mockMvc.perform(get("/api/students/{id}", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Api Test Student"))
                .andExpect(jsonPath("$.gpa").value(nullValue()));
    }

    @Test
    void getStudent_whenMissing_returns404() throws Exception {
        mockMvc.perform(get("/api/students/{id}", 999999L)
                        .with(user("adminUser").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchStudents_byName_returnsOnlyTheMatches() throws Exception {
        mockMvc.perform(get("/api/students").param("name", "Api Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Api Test Student"));
    }

    @Test
    void searchStudents_withoutMatches_returnsAnEmptyArray() throws Exception {
        mockMvc.perform(get("/api/students").param("name", "no-such-name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createStudent_returns201WithDefaultsAndLocation() throws Exception {
        String body = "{\"name\":\"New Api Student\",\"yearOfStudy\":1}";

        mockMvc.perform(post("/api/students")
                        .with(user("owner").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/students/")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("New Api Student"))
                .andExpect(jsonPath("$.gpa").value(0.0));
    }

    @Test
    void createStudent_withInvalidBody_returns400() throws Exception {
        mockMvc.perform(post("/api/students")
                        .with(user("owner").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"yearOfStudy\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }
}
