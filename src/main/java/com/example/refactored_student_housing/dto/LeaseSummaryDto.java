package com.example.refactored_student_housing.dto;

import java.time.LocalDate;

/** One row of the "rental history" table on the student detail page. */
public class LeaseSummaryDto {

    private final Long unitId;
    private final String unitAddress;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final boolean active;

    public LeaseSummaryDto(Long unitId, String unitAddress, LocalDate startDate, LocalDate endDate, boolean active) {
        this.unitId = unitId;
        this.unitAddress = unitAddress;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
    }

    public Long getUnitId() { return unitId; }
    public String getUnitAddress() { return unitAddress; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public boolean isActive() { return active; }
}
