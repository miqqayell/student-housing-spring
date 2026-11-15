package com.example.refactored_student_housing.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StudentCreateDto {
    @NotBlank(message = "Name cannot be empty")
    private String name;

    @NotNull
    @Min(value = 1, message = "Year of study must be at least 1")
    @Max(value = 7, message = "Year of study cannot exceed 7")
    private Integer yearOfStudy;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getYearOfStudy() { return yearOfStudy; }
    public void setYearOfStudy(Integer yearOfStudy) { this.yearOfStudy = yearOfStudy; }
}