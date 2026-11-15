package com.example.refactored_student_housing.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "leases")
public class Lease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate startDate;
    private LocalDate endDate;
    private Double deposit;
    private boolean active;
    private String contractUrl;

    // Lease is the owning side of both associations. Fetching is LAZY and the
    // owning entities are join-fetched by the queries that need them; no
    // cascading, because a lease must never create or delete a student/unit.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unit_id")
    private HousingUnit housingUnit;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Double getDeposit() { return deposit; }
    public void setDeposit(Double deposit) { this.deposit = deposit; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getContractUrl() { return contractUrl; }
    public void setContractUrl(String contractUrl) { this.contractUrl = contractUrl; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public HousingUnit getHousingUnit() { return housingUnit; }
    public void setHousingUnit(HousingUnit housingUnit) { this.housingUnit = housingUnit; }
}
