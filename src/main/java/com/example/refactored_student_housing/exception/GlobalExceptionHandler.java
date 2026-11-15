package com.example.refactored_student_housing.exception;

import com.example.refactored_student_housing.web.HousingUnitController;
import com.example.refactored_student_housing.web.NavController;
import com.example.refactored_student_housing.web.RegistrationController;
import com.example.refactored_student_housing.web.StudentController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.sql.SQLException;

/**
 * Error handling for the server-rendered (MVC) pages only.
 *
 * It is bound to the MVC controllers explicitly so it can never intercept a
 * Web API request and answer an HTML page to a client that asked for JSON -
 * that is what {@link ApiExceptionHandler} is for.
 *
 * Every handler also sets an HTTP status: rendering an error page with status
 * 200 hides failures from browsers, proxies and monitoring.
 */
@ControllerAdvice(assignableTypes = {
        StudentController.class,
        HousingUnitController.class,
        RegistrationController.class,
        NavController.class
})
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", "We could not find what you were looking for.");
        model.addAttribute("technicalDetails", ex.getMessage());
        return "error/generic_error";
    }

    /** The user is signed in but does not own this record (see the @PreAuthorize rules). */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(AccessDeniedException ex, Model model) {
        model.addAttribute("errorMessage", "You are not allowed to perform this action.");
        model.addAttribute("technicalDetails", "Only the advisor who owns this record, or an administrator, may change it.");
        return "error/generic_error";
    }

    /** A business rule refused the request (e.g. a fully occupied unit). */
    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleServiceException(ServiceException ex, Model model) {
        log.warn("Business rule violation: {}", ex.getMessage());
        model.addAttribute("errorMessage", "Action failed");
        model.addAttribute("technicalDetails", ex.getMessage());
        return "error/generic_error";
    }

    @ExceptionHandler({DataAccessException.class, SQLException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleDatabaseException(Exception ex, Model model) {
        log.error("Database error occurred: ", ex);
        model.addAttribute("errorMessage", "A database error occurred. Please contact support.");
        model.addAttribute("technicalDetails", ex.getMessage());
        return "error/database_error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneralException(Exception ex, Model model) {
        log.error("Unhandled exception occurred: ", ex);
        model.addAttribute("errorMessage", "An unexpected error occurred.");
        model.addAttribute("technicalDetails", ex.getMessage());
        return "error/generic_error";
    }
}
