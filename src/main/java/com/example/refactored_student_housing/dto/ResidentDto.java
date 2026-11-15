package com.example.refactored_student_housing.dto;

import java.time.LocalDate;

/**
 * A resident of a housing unit. Whether the viewer may see who this is, is
 * decided server side ({@code nameVisible}) rather than in the template.
 */
public class ResidentDto {

    private final Long studentId;
    private final String studentName;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final boolean active;
    private final boolean nameVisible;

    public ResidentDto(Long studentId, String studentName, LocalDate startDate, LocalDate endDate,
                       boolean active, boolean nameVisible) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
        this.nameVisible = nameVisible;
    }

    public Long getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public boolean isActive() { return active; }
    public boolean isNameVisible() { return nameVisible; }
}
