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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Week 9 - unit tests with mocking for the second business-layer method:
 * the leasing rules that used to live inside the MVC controller.
 */
@ExtendWith(MockitoExtension.class)
class LeaseServiceImplTest {

    @Mock
    private LeaseRepository leaseRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private HousingUnitRepository unitRepository;

    @InjectMocks
    private LeaseServiceImpl service;

    @Captor
    private ArgumentCaptor<Lease> leaseCaptor;

    private HousingUnit unit;
    private Student student;
    private LeaseFormDto form;

    @BeforeEach
    void setUp() {
        unit = new HousingUnit();
        unit.setId(1L);
        unit.setAddress("Test Street 1");
        unit.setCapacity(2);

        student = new Student();
        student.setId(2L);
        student.setName("Renting Student");

        form = new LeaseFormDto();
        form.setStudentId(2L);
        form.setStartDate(LocalDate.of(2026, 9, 1));
        form.setEndDate(LocalDate.of(2027, 6, 30));
    }

    @Test
    void rentUnit_whenRoomIsAvailable_savesAnActiveLease() {
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(leaseRepository.countByHousingUnitIdAndActiveTrue(1L)).thenReturn(1L);
        when(leaseRepository.existsByStudentIdAndActiveTrue(2L)).thenReturn(false);

        service.rentUnit(1L, form);

        verify(leaseRepository).save(leaseCaptor.capture());
        Lease saved = leaseCaptor.getValue();
        assertEquals(student, saved.getStudent());
        assertEquals(unit, saved.getHousingUnit());
        assertTrue(saved.isActive());
        assertEquals(500.0, saved.getDeposit(), "An omitted deposit falls back to the standard amount");
    }

    @Test
    void rentUnit_whenTheUnitIsFull_isRefused() {
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(leaseRepository.countByHousingUnitIdAndActiveTrue(1L)).thenReturn(2L); // capacity is 2

        ServiceException ex = assertThrows(ServiceException.class, () -> service.rentUnit(1L, form));

        assertEquals("This unit is fully occupied!", ex.getMessage());
        verify(leaseRepository, never()).save(any(Lease.class));
    }

    @Test
    void rentUnit_whenTheStudentAlreadyRents_isRefused() {
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(leaseRepository.countByHousingUnitIdAndActiveTrue(1L)).thenReturn(0L);
        when(leaseRepository.existsByStudentIdAndActiveTrue(2L)).thenReturn(true);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.rentUnit(1L, form));

        assertEquals("Student already has an active lease!", ex.getMessage());
        verify(leaseRepository, never()).save(any(Lease.class));
    }

    @Test
    void rentUnit_whenTheUnitDoesNotExist_throwsNotFound() {
        when(unitRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.rentUnit(99L, form));

        verify(leaseRepository, never()).save(any(Lease.class));
    }

    @Test
    void createStandardLease_usesAOneYearActiveLease() {
        lenient().when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student));
        when(leaseRepository.countByHousingUnitIdAndActiveTrue(1L)).thenReturn(0L);
        when(leaseRepository.existsByStudentIdAndActiveTrue(2L)).thenReturn(false);

        service.createStandardLease(2L, 1L);

        verify(leaseRepository).save(leaseCaptor.capture());
        Lease saved = leaseCaptor.getValue();
        assertEquals(LocalDate.now(), saved.getStartDate());
        assertEquals(LocalDate.now().plusYears(1), saved.getEndDate());
        assertTrue(saved.isActive());
    }
}
