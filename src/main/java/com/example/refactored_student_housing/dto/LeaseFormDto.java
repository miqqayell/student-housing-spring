package com.example.refactored_student_housing.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Backing object of the "rent a unit" form.
 *
 * Previously the JPA entity itself was bound to this form, which let a request
 * write to any entity property. Binding a dedicated DTO keeps the entity out of
 * the request and makes the accepted input explicit.
 */
public class LeaseFormDto {

    @NotNull(message = "Please select a student")
    private Long studentId;

    @NotNull(message = "A start date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @NotNull(message = "An end date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @PositiveOrZero(message = "The deposit cannot be negative")
    private Double deposit;

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Double getDeposit() { return deposit; }
    public void setDeposit(Double deposit) { this.deposit = deposit; }
}
