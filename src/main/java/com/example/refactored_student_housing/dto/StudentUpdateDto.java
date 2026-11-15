package com.example.refactored_student_housing.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull; // Add this import

public class StudentUpdateDto {

    @NotNull(message = "GPA is required")
    @DecimalMin(value = "0.0", message = "GPA must be positive")
    @DecimalMax(value = "4.0", message = "GPA cannot exceed 4.0")
    private Double gpa;

    public Double getGpa() { return gpa; }
    public void setGpa(Double gpa) { this.gpa = gpa; }
}