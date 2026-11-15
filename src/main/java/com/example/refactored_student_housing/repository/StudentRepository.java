package com.example.refactored_student_housing.repository;

import com.example.refactored_student_housing.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * All filtering happens in the database (WHERE clauses), never by loading every
 * row and filtering in Java. Associations needed by the caller are pulled in
 * with JOIN FETCH so a page render costs a single query, which is required now
 * that open-in-view is disabled.
 */
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * One parameterised query backing every student listing (free search,
     * "international only", "top GPA") and the role-based restriction to the
     * records of a single advisor. A null parameter simply disables that filter.
     *
     * {@code namePattern} is a ready-made LIKE pattern; build it with
     * {@link SearchPatterns#contains(String)}.
     */
    @Query("""
            SELECT s FROM Student s
            LEFT JOIN FETCH s.advisor a
            WHERE LOWER(s.name) LIKE LOWER(:namePattern)
              AND (:minYear IS NULL OR s.yearOfStudy >= :minYear)
              AND (:bornBefore IS NULL OR s.birthDate < :bornBefore)
              AND (:minGpa IS NULL OR s.gpa >= :minGpa)
              AND (:internationalOnly = FALSE OR s.international = TRUE)
              AND (:advisorUsername IS NULL OR a.username = :advisorUsername)
            ORDER BY s.name
            """)
    List<Student> search(@Param("namePattern") String namePattern,
                         @Param("minYear") Integer minYear,
                         @Param("bornBefore") LocalDate bornBefore,
                         @Param("minGpa") Double minGpa,
                         @Param("internationalOnly") boolean internationalOnly,
                         @Param("advisorUsername") String advisorUsername);


    /** Everything the detail page renders, in one query. */
    @Query("""
            SELECT DISTINCT s FROM Student s
            LEFT JOIN FETCH s.advisor
            LEFT JOIN FETCH s.leases l
            LEFT JOIN FETCH l.housingUnit
            WHERE s.id = :id
            """)
    Optional<Student> findDetailById(@Param("id") Long id);

    /** The owner is needed for authorization checks, the leases are not. */
    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.advisor WHERE s.id = :id")
    Optional<Student> findWithAdvisorById(@Param("id") Long id);

    /** Ownership check done by the database, used by the @PreAuthorize rules. */
    @Query("""
            SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END
            FROM Student s
            WHERE s.id = :id AND s.advisor.username = :username
            """)
    boolean isOwnedBy(@Param("id") Long id, @Param("username") String username);
}
