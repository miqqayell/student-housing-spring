package com.example.refactored_student_housing.dto;

/**
 * Response body returned by the housing-unit creation endpoint (POST /api/units).
 * Carries the generated id back to the client and avoids serializing the lazy
 * 'leases' association of the entity.
 */
public class HousingUnitResponseDto {

    private Long id;
    private String address;
    private Integer capacity;
    private boolean furnished;
    private Double rent;
    private String unitType;
    private String imageUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
