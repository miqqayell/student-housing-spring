package com.example.refactored_student_housing.dto;

import java.util.List;

/** Everything the unit detail page renders, including its residents. */
public class HousingUnitDetailDto {

    private final Long id;
    private final String address;
    private final Integer capacity;
    private final Double rent;
    private final String unitType;
    private final String imageUrl;
    private final boolean furnished;
    private final String managerName;
    private final List<ResidentDto> residents;

    public HousingUnitDetailDto(Long id, String address, Integer capacity, Double rent, String unitType,
                                String imageUrl, boolean furnished, String managerName, List<ResidentDto> residents) {
        this.id = id;
        this.address = address;
        this.capacity = capacity;
        this.rent = rent;
        this.unitType = unitType;
        this.imageUrl = imageUrl;
        this.furnished = furnished;
        this.managerName = managerName;
        this.residents = residents;
    }

    public Long getId() { return id; }
    public String getAddress() { return address; }
    public Integer getCapacity() { return capacity; }
    public Double getRent() { return rent; }
    public String getUnitType() { return unitType; }
    public String getImageUrl() { return imageUrl; }
    public boolean isFurnished() { return furnished; }
    public String getManagerName() { return managerName; }
    public List<ResidentDto> getResidents() { return residents; }
}
