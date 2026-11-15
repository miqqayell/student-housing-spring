package com.example.refactored_student_housing.repository;

import com.example.refactored_student_housing.domain.HousingUnit;
import com.example.refactored_student_housing.domain.Lease;
import com.example.refactored_student_housing.domain.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Week 6 - repository layer tests.
 *
 * Covers how deletes are handled (leases are cascaded away with their student)
 * and that the database-side search really filters instead of returning
 * everything.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private LeaseRepository leaseRepository;

    @Autowired
    private HousingUnitRepository unitRepository;

    private Student savedStudent;
    private Long leaseId;

    @BeforeEach
    void setUp() {
        HousingUnit unit = new HousingUnit();
        unit.setAddress("Cascade Street 1");
        unit.setCapacity(2);
        unit.setRent(400.0);
        unit.setUnitType("APARTMENT");
        unitRepository.saveAndFlush(unit);

        Student student = new Student();
        student.setName("Cascade Test Student");
        student.setYearOfStudy(1);
        student.setInternational(false);
        student.setGpa(3.0);
        student.setBirthDate(LocalDate.of(2000, 1, 1));

        Lease lease = new Lease();
        lease.setHousingUnit(unit);
        lease.setStartDate(LocalDate.now());
        lease.setActive(true);
        // Keeps both sides of the association consistent before cascading the save.
        student.addLease(lease);

        savedStudent = studentRepository.saveAndFlush(student);
        leaseId = savedStudent.getLeases().getFirst().getId();
    }

    @Test
    void deletingAStudent_alsoDeletesItsLeases() {
        studentRepository.deleteById(savedStudent.getId());
        studentRepository.flush();

        assertTrue(studentRepository.findById(savedStudent.getId()).isEmpty(),
                "Student should be deleted from the database");
        assertTrue(leaseRepository.findById(leaseId).isEmpty(),
                "A lease has no meaning without its student, so it must be cascaded away");
    }

    @Test
    void search_filtersByNameInTheDatabase() {
        var results = studentRepository.search("%cascade test%", null, null, null, false, null);

        assertEquals(1, results.size());
        assertEquals("Cascade Test Student", results.getFirst().getName());
    }

    @Test
    void search_withoutMatches_returnsNothing() {
        assertTrue(studentRepository.search("%no-such-student%", null, null, null, false, null).isEmpty());
    }
}
