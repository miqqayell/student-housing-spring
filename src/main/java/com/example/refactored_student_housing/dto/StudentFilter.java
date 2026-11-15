package com.example.refactored_student_housing.dto;

import java.time.LocalDate;

/**
 * Search criteria for a student listing. Every field is optional; a null value
 * means "don't filter on this". The filter is translated into a single database
 * query - no listing is ever filtered in memory.
 *
 * @param restrictToAdvisor when set, only the records owned by that user are
 *                          returned. This is the database-side implementation of
 *                          the week 5 rule that a non-admin only manages their
 *                          own records.
 */
public record StudentFilter(String nameContains,
                            Integer minYear,
                            LocalDate bornBefore,
                            Double minGpa,
                            boolean internationalOnly,
                            String restrictToAdvisor) {

    public static StudentFilter of(String nameContains, Integer minYear, LocalDate bornBefore) {
        return new StudentFilter(blankToNull(nameContains), minYear, bornBefore, null, false, null);
    }

    public static StudentFilter onlyInternational() {
        return new StudentFilter(null, null, null, null, true, null);
    }

    public static StudentFilter withMinGpa(double minGpa) {
        return new StudentFilter(null, null, null, minGpa, false, null);
    }

    public StudentFilter restrictedTo(String advisorUsername) {
        return new StudentFilter(nameContains, minYear, bornBefore, minGpa, internationalOnly, advisorUsername);
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
