package com.example.refactored_student_housing.dto;

import jakarta.validation.constraints.*;

public class HousingUnitDto {

    @NotEmpty(message = "Address is required")
    private String address;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private boolean furnished;

    @NotNull(message = "Rent is required")
    @Positive(message = "Rent must be positive")
    private Double rent;

    @NotEmpty(message = "Unit type is required")
    private String unitType;

    private String imageUrl;

    // Getters and Setters
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public boolean isFurnished() { return furnished; }
    public void setFurnished(boolean furnished) { this.furnished = furnished; }
    public Double getRent() { return rent; }
    public void setRent(Double rent) { this.rent = rent; }

    public String getUnitType() { return unitType; }
    public void setUnitType(String unitType) { this.unitType = unitType; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}