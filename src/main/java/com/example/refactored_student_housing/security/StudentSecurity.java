package com.example.refactored_student_housing.security;

import com.example.refactored_student_housing.repository.StudentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The "complex authorization" rule of week 5, expressed once and referenced from
 * the {@code @PreAuthorize} expressions on the service layer:
 * a student record may only be modified by the advisor that owns it (or by an
 * administrator, which the expression itself handles).
 *
 * Keeping this in one place means the MVC pages and the Web API are guaranteed
 * to enforce exactly the same rule.
 */
@Component("studentSecurity")
public class StudentSecurity {

    private final StudentRepository studentRepository;

    public StudentSecurity(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public boolean mayModify(Long studentId, Authentication authentication) {
        if (studentId == null || authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        // A record that does not exist is not an authorization problem: allow the
        // call through so the service can answer 404 instead of a misleading 403.
        if (!studentRepository.existsById(studentId)) {
            return true;
        }
        return studentRepository.isOwnedBy(studentId, authentication.getName());
    }
}
