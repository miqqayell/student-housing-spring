package com.example.refactored_student_housing.web.api;

import com.example.refactored_student_housing.dto.StudentCreateDto;
import com.example.refactored_student_housing.dto.StudentDto;
import com.example.refactored_student_housing.dto.StudentSearchDto;
import com.example.refactored_student_housing.dto.StudentUpdateDto;
import com.example.refactored_student_housing.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Web API for students.
 *
 * REST-wise: the collection lives at /api/students, a single record at
 * /api/students/{id}; the verb expresses the operation (GET/POST/PATCH/DELETE);
 * the status code expresses the outcome (200/201/204, and 400/403/404 through
 * the {@code ApiExceptionHandler}); a created resource is advertised with a
 * Location header; and every method declares what it consumes/produces.
 *
 * The controller itself holds no logic: it binds the request, delegates, and
 * maps the result onto an HTTP response.
 */
@RestController
@RequestMapping("/api/students")
public class StudentApiController {

    private final StudentService studentService;

    public StudentApiController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * Collection lookup with an optional {@code name} query parameter, consumed
     * by the separate Client project. Returns a public-safe projection.
     */
    @GetMapping(produces = "application/json")
    public List<StudentSearchDto> searchStudents(@RequestParam(required = false) String name) {
        return studentService.searchByName(name);
    }

    /** 200 with the record, or 404 when it does not exist. */
    @GetMapping(value = "/{id}", produces = "application/json")
    public StudentDto getStudent(@PathVariable Long id, @AuthenticationPrincipal UserDetails currentUser) {
        return studentService.findStudentForApi(id, currentUser);
    }

    /** 201 Created, with a Location header pointing at the new record. */
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<StudentDto> createStudent(@Valid @RequestBody StudentCreateDto dto,
                                                    @AuthenticationPrincipal UserDetails currentUser) {
        StudentDto created = studentService.createFromApi(dto, currentUser == null ? null : currentUser.getUsername());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    /**
     * Partial update. Only the owning advisor or an administrator may do this;
     * that rule is enforced by {@code @PreAuthorize} on the service and surfaces
     * here as a 403.
     */
    @PatchMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    public StudentDto updateStudentGpa(@PathVariable Long id, @Valid @RequestBody StudentUpdateDto dto) {
        return studentService.updateGpa(id, dto);
    }

    /** 204 No Content; same ownership rule as the update. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
