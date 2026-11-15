package com.example.refactored_student_housing.service;

import com.example.refactored_student_housing.domain.HousingUnit;
import com.example.refactored_student_housing.domain.Lease;
import com.example.refactored_student_housing.domain.Student;
import com.example.refactored_student_housing.dto.LeaseFormDto;
import com.example.refactored_student_housing.exception.ResourceNotFoundException;
import com.example.refactored_student_housing.exception.ServiceException;
import com.example.refactored_student_housing.repository.HousingUnitRepository;
import com.example.refactored_student_housing.repository.LeaseRepository;
import com.example.refactored_student_housing.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * All leasing rules live here. They used to sit in the MVC controller, which
 * meant the Web API could have bypassed them entirely.
 */
@Service
@Transactional
public class LeaseServiceImpl implements LeaseService {

    private static final double DEFAULT_DEPOSIT = 500.0;
    private static final int STANDARD_LEASE_YEARS = 1;

    private final LeaseRepository leaseRepository;
    private final StudentRepository studentRepository;
    private final HousingUnitRepository unitRepository;

    public LeaseServiceImpl(LeaseRepository leaseRepository,
                            StudentRepository studentRepository,
                            HousingUnitRepository unitRepository) {
        this.leaseRepository = leaseRepository;
        this.studentRepository = studentRepository;
        this.unitRepository = unitRepository;
    }

    @Override
    public void rentUnit(Long unitId, LeaseFormDto form) {
        HousingUnit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> ResourceNotFoundException.of("Housing unit", unitId));
        Student student = studentRepository.findById(form.getStudentId())
                .orElseThrow(() -> ResourceNotFoundException.of("Student", form.getStudentId()));

        assertUnitHasRoom(unit);
        assertStudentIsFree(student.getId());

        Lease lease = new Lease();
        lease.setStudent(student);
        lease.setHousingUnit(unit);
        lease.setStartDate(form.getStartDate());
        lease.setEndDate(form.getEndDate());
        lease.setDeposit(form.getDeposit() == null ? DEFAULT_DEPOSIT : form.getDeposit());
        lease.setActive(true);

        leaseRepository.save(lease);
    }

    @Override
    public void createStandardLease(Long studentId, Long unitId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Student", studentId));
        HousingUnit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> ResourceNotFoundException.of("Housing unit", unitId));

        assertUnitHasRoom(unit);
        assertStudentIsFree(studentId);

        Lease lease = new Lease();
        lease.setStudent(student);
        lease.setHousingUnit(unit);
        lease.setStartDate(LocalDate.now());
        lease.setEndDate(LocalDate.now().plusYears(STANDARD_LEASE_YEARS));
        lease.setDeposit(DEFAULT_DEPOSIT);
        lease.setActive(true);

        leaseRepository.save(lease);
    }

    /** Counted by the database rather than by loading every lease of the unit. */
    private void assertUnitHasRoom(HousingUnit unit) {
        long occupied = leaseRepository.countByHousingUnitIdAndActiveTrue(unit.getId());
        if (unit.getCapacity() != null && occupied >= unit.getCapacity()) {
            throw new ServiceException("This unit is fully occupied!");
        }
    }

    private void assertStudentIsFree(Long studentId) {
        if (leaseRepository.existsByStudentIdAndActiveTrue(studentId)) {
            throw new ServiceException("Student already has an active lease!");
        }
    }
}
