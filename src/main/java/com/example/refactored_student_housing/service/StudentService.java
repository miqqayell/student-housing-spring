package com.example.refactored_student_housing.service;

import com.example.refactored_student_housing.dto.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

/**
 * Business layer for students. Everything the presentation layer receives is a
 * DTO that was fully assembled inside a transaction, so no controller or
 * template can trigger lazy loading (open-in-view is disabled).
 */
public interface StudentService {

    /** Listing for the MVC overview. Non-admins only see the records they own. */
    List<StudentListItemDto> findStudents(StudentFilter filter, UserDetails currentUser);

    /** Detail page data, including the rental history, in a single query. */
    StudentDetailDto findStudentDetail(Long id, UserDetails currentUser);

    /** Web API read model; the GPA is stripped for callers that may not see it. */
    StudentDto findStudentForApi(Long id, UserDetails currentUser);

    /** Public search endpoint used by the separate Client project (cached). */
    List<StudentSearchDto> searchByName(String name);

    /** Creates a student from the MVC form, optionally renting a unit right away. */
    Long createFromForm(StudentFormDto form, String advisorUsername);

    /** Creates a student from the Web API, applying the API defaults. */
    StudentDto createFromApi(StudentCreateDto dto, String advisorUsername);

    StudentDto updateGpa(Long id, StudentUpdateDto dto);

    void deleteById(Long id);

    /** Bulk creation for the asynchronous CSV import; returns the number created. */
    int importStudents(List<StudentImportRow> rows);
}
