package com.example.refactored_student_housing.dto;

import java.time.LocalDate;

/**
 * One parsed line of the bulk import CSV (week 12). The import service is only
 * responsible for turning text into these rows; creating and persisting the
 * domain objects stays in {@code StudentService}.
 */
public record StudentImportRow(String name,
                               LocalDate birthDate,
                               int yearOfStudy,
                               Double gpa,
                               boolean international) {
}
