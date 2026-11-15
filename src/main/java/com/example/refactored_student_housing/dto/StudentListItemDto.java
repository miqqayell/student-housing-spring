package com.example.refactored_student_housing.dto;

/**
 * What the students overview page renders for one student.
 *
 * The presentation layer receives this instead of the entity: it is assembled
 * inside the service transaction, so no lazy association is ever touched while
 * rendering (open-in-view is disabled). The visibility/permission flags are
 * decided server side, so the template only has to render them.
 */
public class StudentListItemDto {

    private final Long id;
    private final String name;
    private final int yearOfStudy;
    private final Double gpa;
    private final boolean gpaVisible;
    private final boolean international;
    private final String profileImageUrl;
    private final boolean manageable;

    public StudentListItemDto(Long id, String name, int yearOfStudy, Double gpa, boolean gpaVisible,
                              boolean international, String profileImageUrl, boolean manageable) {
        this.id = id;
        this.name = name;
        this.yearOfStudy = yearOfStudy;
        this.gpa = gpa;
        this.gpaVisible = gpaVisible;
        this.international = international;
        this.profileImageUrl = profileImageUrl;
        this.manageable = manageable;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public int getYearOfStudy() { return yearOfStudy; }
    public Double getGpa() { return gpa; }
    public boolean isGpaVisible() { return gpaVisible; }
    public boolean isInternational() { return international; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public boolean isManageable() { return manageable; }

    /** Convenience for the "High GPA" badge; never NPEs on a missing GPA. */
    public boolean isHighGpa() { return gpa != null && gpa >= 3.5; }
}
