package com.example.refactored_student_housing.web.api;

import com.example.refactored_student_housing.dto.StudentDto;
import com.example.refactored_student_housing.dto.StudentUpdateDto;
import com.example.refactored_student_housing.exception.ResourceNotFoundException;
import com.example.refactored_student_housing.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Week 9 - unit (slice) test for a single Web API endpoint:
 * PATCH /api/students/{id}.
 *
 * The controller's only collaborator is mocked with {@code @MockBean}, so this
 * exercises the controller and the HTTP layer around it: request binding,
 * validation, and the translation of each service outcome onto a status code.
 * Both the happy path and every failure path are covered.
 */
@WebMvcTest(StudentApiController.class)
class StudentApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    private static final String VALID_BODY = "{\"gpa\":3.9}";

    private StudentDto returned;

    @BeforeEach
    void setUp() {
        returned = new StudentDto();
        returned.setId(1L);
        returned.setName("Mocked Student");
        returned.setGpa(3.9);
    }

    @Test
    void updateGpa_whenAllowed_returns200AndTheUpdatedRecord() throws Exception {
        when(studentService.updateGpa(eq(1L), any(StudentUpdateDto.class))).thenReturn(returned);

        mockMvc.perform(patch("/api/students/{id}", 1L)
                        .with(user("owner").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gpa").value(3.9));

        // the parsed body must reach the service unchanged
        verify(studentService).updateGpa(eq(1L), any(StudentUpdateDto.class));
    }

    @Test
    void updateGpa_whenTheServiceRefuses_returns403() throws Exception {
        // The ownership rule is enforced by @PreAuthorize on the service.
        when(studentService.updateGpa(eq(1L), any(StudentUpdateDto.class)))
                .thenThrow(new AccessDeniedException("not the owner"));

        mockMvc.perform(patch("/api/students/{id}", 1L)
                        .with(user("intruder").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateGpa_whenTheStudentIsMissing_returns404() throws Exception {
        when(studentService.updateGpa(eq(1L), any(StudentUpdateDto.class)))
                .thenThrow(ResourceNotFoundException.of("Student", 1L));

        mockMvc.perform(patch("/api/students/{id}", 1L)
                        .with(user("owner").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateGpa_withAMissingGpa_returns400AndNeverCallsTheService() throws Exception {
        // gpa is @NotNull on the update DTO -> validation fails before any business logic
        mockMvc.perform(patch("/api/students/{id}", 1L)
                        .with(user("owner").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.gpa").exists());

        verify(studentService, never()).updateGpa(anyLong(), any(StudentUpdateDto.class));
    }

    @Test
    void updateGpa_withAnOutOfRangeGpa_returns400() throws Exception {
        mockMvc.perform(patch("/api/students/{id}", 1L)
                        .with(user("owner").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gpa\":5.5}"))
                .andExpect(status().isBadRequest());

        verify(studentService, never()).updateGpa(anyLong(), any(StudentUpdateDto.class));
    }

    @Test
    void createStudent_returns201WithALocationHeader() throws Exception {
        StudentDto created = new StudentDto();
        created.setId(7L);
        created.setName("Eve Developer");

        when(studentService.createFromApi(any(), eq("owner"))).thenReturn(created);

        mockMvc.perform(post("/api/students")
                        .with(user("owner").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Eve Developer\",\"yearOfStudy\":1}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/students/7"))
                .andExpect(jsonPath("$.id").value(7));
    }
}
