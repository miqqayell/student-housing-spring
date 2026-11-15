package com.example.refactored_student_housing.dto;

import java.time.LocalDate;

/**
 * Read model returned by the Web API (GET/POST/PATCH /api/students).
 *
 * A response model carries no validation constraints - those live on the
 * request DTOs ({@link StudentCreateDto}, {@link StudentUpdateDto}). A null
 * {@code gpa} means the caller is not allowed to see it.
 */
public class StudentDto {

    private Long id;
    private String name;
    private LocalDate birthDate;
    private int yearOfStudy;
    private Double gpa;
    private boolean international;
    private int leaseCount;
    private String profileImageUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public int getLeaseCount() { return leaseCount; }
    public void setLeaseCount(int leaseCount) { this.leaseCount = leaseCount; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
}
