package com.example.refactored_student_housing.repository;

import com.example.refactored_student_housing.domain.Lease;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaseRepository extends JpaRepository<Lease, Long> {

    /**
     * Occupancy check done by the database with a COUNT, instead of loading
     * every lease of the unit into memory to count them there.
     */
    long countByHousingUnitIdAndActiveTrue(Long unitId);

    boolean existsByStudentIdAndActiveTrue(Long studentId);
}
