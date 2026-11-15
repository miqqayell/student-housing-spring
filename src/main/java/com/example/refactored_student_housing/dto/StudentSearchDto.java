package com.example.refactored_student_housing.dto;

/**
 * Lightweight, public-safe projection of a Student used by the search endpoint
 * (GET /api/students?name=...). It intentionally omits the GPA, which is only
 * exposed through the authenticated GET /api/students/{id} endpoint.
 */
public class StudentSearchDto {

    private Long id;
    private String name;
    private int yearOfStudy;
    private boolean international;

    public StudentSearchDto() {
    }

    public StudentSearchDto(Long id, String name, int yearOfStudy, boolean international) {
        this.id = id;
        this.name = name;
        this.yearOfStudy = yearOfStudy;
        this.international = international;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getYearOfStudy() { return yearOfStudy; }
    public void setYearOfStudy(int yearOfStudy) { this.yearOfStudy = yearOfStudy; }
    public boolean isInternational() { return international; }
    public void setInternational(boolean international) { this.international = international; }
}
