package com.example.refactored_student_housing.dto;

import java.time.LocalDate;
import java.util.List;

/** Everything the student detail page renders, assembled inside the service transaction. */
public class StudentDetailDto {

    private final Long id;
    private final String name;
    private final LocalDate birthDate;
    private final int yearOfStudy;
    private final Double gpa;
    private final boolean gpaVisible;
    private final boolean international;
    private final String profileImageUrl;
    private final boolean manageable;
    private final List<LeaseSummaryDto> leases;

    public StudentDetailDto(Long id, String name, LocalDate birthDate, int yearOfStudy, Double gpa,
                            boolean gpaVisible, boolean international, String profileImageUrl,
                            boolean manageable, List<LeaseSummaryDto> leases) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
        this.yearOfStudy = yearOfStudy;
        this.gpa = gpa;
        this.gpaVisible = gpaVisible;
        this.international = international;
        this.profileImageUrl = profileImageUrl;
        this.manageable = manageable;
        this.leases = leases;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public LocalDate getBirthDate() { return birthDate; }
    public int getYearOfStudy() { return yearOfStudy; }
    public Double getGpa() { return gpa; }
    public boolean isGpaVisible() { return gpaVisible; }
    public boolean isInternational() { return international; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public boolean isManageable() { return manageable; }
    public List<LeaseSummaryDto> getLeases() { return leases; }

    public boolean isHighGpa() { return gpa != null && gpa >= 3.5; }
}
