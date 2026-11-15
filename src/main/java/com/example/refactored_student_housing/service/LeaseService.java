package com.example.refactored_student_housing.service;

import com.example.refactored_student_housing.dto.LeaseFormDto;

public interface LeaseService {

    /**
     * Rents a unit to a student. Enforces the two occupancy rules of the domain:
     * a unit cannot exceed its capacity, and a student cannot hold two active
     * leases at the same time.
     */
    void rentUnit(Long unitId, LeaseFormDto form);

    /** Standard one-year active lease, used when a unit is picked while adding a student. */
    void createStandardLease(Long studentId, Long unitId);
}
