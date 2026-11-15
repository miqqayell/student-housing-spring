package com.example.refactored_student_housing.repository;

/**
 * Builds the LIKE patterns used by the "contains" filters.
 *
 * The queries always bind a pattern instead of a nullable fragment: PostgreSQL
 * cannot infer the type of a null argument passed to lower()/concat(), which
 * makes such a query fail as soon as the search box is left empty.
 */
public final class SearchPatterns {

    private SearchPatterns() {
    }

    /** "%" matches everything, so an absent search term disables the filter. */
    public static String contains(String term) {
        return (term == null || term.isBlank()) ? "%" : "%" + term.trim() + "%";
    }
}
