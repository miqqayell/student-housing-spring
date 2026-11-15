package com.example.refactored_student_housing.web;

import com.example.refactored_student_housing.domain.AppUser;
import com.example.refactored_student_housing.domain.Student;
import com.example.refactored_student_housing.repository.AppUserRepository;
import com.example.refactored_student_housing.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Week 8 - MVC integration tests for the presentation layer.
 * Covers GET /students (role-based list filtering and GPA visibility),
 * GET /students/{id} (path variable + model), GET /students/top (filtered view)
 * and the 404 path.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StudentMvcControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AppUserRepository userRepository;

    private Long ownedStudentId;

    @BeforeEach
    void setUp() {
        AppUser owner = createUser("mvcOwner", "USER");
        AppUser other = createUser("otherOwner", "USER");

        // Owned by mvcOwner, high GPA -> appears in "top students".
        Student owned = new Student();
        owned.setName("Owned Student");
        owned.setYearOfStudy(3);
        owned.setBirthDate(LocalDate.of(2001, 1, 1));
        owned.setInternational(false);
        owned.setGpa(3.9);
        owned.setAdvisor(owner);
        ownedStudentId = studentRepository.saveAndFlush(owned).getId();

        // Owned by someone else, low GPA -> excluded from "top students".
        Student foreign = new Student();
        foreign.setName("Other Student");
        foreign.setYearOfStudy(2);
        foreign.setBirthDate(LocalDate.of(2003, 5, 5));
        foreign.setInternational(false);
        foreign.setGpa(3.0);
        foreign.setAdvisor(other);
        studentRepository.saveAndFlush(foreign);
    }

    private AppUser createUser(String username, String role) {
        AppUser u = new AppUser();
        u.setUsername(username);
        u.setPassword("pw");
        u.setEmail(username + "@test.com");
        u.setAge(30);
        u.setRole(role);
        return userRepository.saveAndFlush(u);
    }

    @Test
    void list_asAdmin_showsAllStudents() throws Exception {
        mockMvc.perform(get("/students").with(user("adminUser").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("students/list"))
                .andExpect(model().attribute("students", hasSize(2)));
    }

    @Test
    void list_asStandardUser_showsOnlyOwnedStudents() throws Exception {
        mockMvc.perform(get("/students").with(user("mvcOwner").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(view().name("students/list"))
                .andExpect(model().attribute("students", hasSize(1)))
                .andExpect(model().attribute("students", contains(hasProperty("name", is("Owned Student")))));
    }

    @Test
    void list_withANameFilter_isFilteredByTheQuery() throws Exception {
        mockMvc.perform(get("/students").param("nameContains", "Other")
                        .with(user("adminUser").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("students", hasSize(1)))
                .andExpect(model().attribute("students", contains(hasProperty("name", is("Other Student")))));
    }

    @Test
    void list_asAnonymous_hidesEveryGpa() throws Exception {
        mockMvc.perform(get("/students"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("students", hasSize(2)))
                .andExpect(model().attribute("students",
                        org.hamcrest.Matchers.everyItem(hasProperty("gpaVisible", is(false)))));
    }

    @Test
    void detail_returnsStudentViewForPathVariable() throws Exception {
        mockMvc.perform(get("/students/{id}", ownedStudentId))
                .andExpect(status().isOk())
                .andExpect(view().name("students/detail"))
                .andExpect(model().attribute("student", hasProperty("id", is(ownedStudentId))))
                .andExpect(model().attribute("student", hasProperty("name", is("Owned Student"))));
    }

    @Test
    void detail_whenTheStudentDoesNotExist_returns404() throws Exception {
        mockMvc.perform(get("/students/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/generic_error"));
    }

    @Test
    void topStudents_returnsOnlyHighGpaStudents() throws Exception {
        mockMvc.perform(get("/students/top").with(user("adminUser").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("students/list"))
                .andExpect(model().attribute("students", hasSize(1)))
                .andExpect(model().attribute("students", contains(hasProperty("name", is("Owned Student")))));
    }
}
