package com.example.refactored_student_housing.repository;

import com.example.refactored_student_housing.domain.HousingUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HousingUnitRepository extends JpaRepository<HousingUnit, Long> {

    /**
     * Database-side filtering. A null {@code minCapacity} disables that filter;
     * {@code addressPattern} is a ready-made LIKE pattern ("%" matches all), see
     * {@link StudentRepository#containsPattern(String)}.
     */
    @Query("""
            SELECT u FROM HousingUnit u
            WHERE (:minCapacity IS NULL OR u.capacity >= :minCapacity)
              AND LOWER(u.address) LIKE LOWER(:addressPattern)
            ORDER BY u.address
            """)
    List<HousingUnit> search(@Param("minCapacity") Integer minCapacity,
                            @Param("addressPattern") String addressPattern);

    /** The unit plus its residents (and their owners) in a single query. */
    @Query("""
            SELECT DISTINCT u FROM HousingUnit u
            LEFT JOIN FETCH u.leases l
            LEFT JOIN FETCH l.student s
            LEFT JOIN FETCH s.advisor
            WHERE u.id = :id
            """)
    Optional<HousingUnit> findDetailById(@Param("id") Long id);

    @Query("SELECT u FROM HousingUnit u ORDER BY u.address")
    List<HousingUnit> findAllOrderedByAddress();
}
