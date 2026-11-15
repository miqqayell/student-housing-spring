package com.example.refactored_student_housing.exception;

/**
 * Thrown by the service layer when a requested record does not exist.
 * Translated to 404 by the exception handlers, so controllers no longer have to
 * build "not found" responses themselves.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String type, Object id) {
        return new ResourceNotFoundException(type + " with id " + id + " was not found.");
    }
}
