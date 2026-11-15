package com.example.refactored_student_housing.dto;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Backing object of the server-rendered "add student" form.
 *
 * Deliberately separate from {@link StudentDto} (the API read model): a form
 * carries only the fields a user may submit, with the validation rules that
 * belong to that form. The controller binds it and hands it straight to the
 * service, which is where the entity is created.
 */
public class StudentFormDto {

    @NotEmpty(message = "{validation.student.name.notEmpty}")
    @Size(min = 2, max = 50, message = "{validation.student.name.size}")
    private String name;

    @NotNull(message = "{validation.student.birthDate.notNull}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Past(message = "{validation.student.birthDate.past}")
    private LocalDate birthDate;

    @Min(value = 1, message = "{validation.student.year.min}")
    @Max(value = 6, message = "{validation.student.year.max}")
    private int yearOfStudy;

    @DecimalMin(value = "0.0", message = "{validation.student.gpa.min}")
    @DecimalMax(value = "4.0", message = "{validation.student.gpa.max}")
    private Double gpa;

    private boolean international;
    private String profileImageUrl;

    /** Optional unit to immediately rent to the new student. */
    private Long linkUnitId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public int getYearOfStudy() { return yearOfStudy; }
    public void setYearOfStudy(int yearOfStudy) { this.yearOfStudy = yearOfStudy; }
    public Double getGpa() { return gpa; }
    public void setGpa(Double gpa) { this.gpa = gpa; }
    public boolean isInternational() { return international; }
    public void setInternational(boolean international) { this.international = international; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public Long getLinkUnitId() { return linkUnitId; }
    public void setLinkUnitId(Long linkUnitId) { this.linkUnitId = linkUnitId; }
}
