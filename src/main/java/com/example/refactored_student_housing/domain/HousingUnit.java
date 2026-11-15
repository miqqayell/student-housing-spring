package com.example.refactored_student_housing.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "housing_units")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "unit_type_disc", discriminatorType = DiscriminatorType.STRING)
public class HousingUnit {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @Column(nullable = false)
 private String address;

 private Integer capacity;
 private boolean furnished;
 private Double rent;
 private String imageUrl;
 private String unitType;

 // No cascading: deleting a unit must never silently delete lease history.
 // LAZY by default and join-fetched where the leases are actually rendered.
 @OneToMany(mappedBy = "housingUnit", fetch = FetchType.LAZY)
 private List<Lease> leases = new ArrayList<>();

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

 public String getImageUrl() { return imageUrl; }
 public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

 public String getUnitType() { return unitType; }
 public void setUnitType(String unitType) { this.unitType = unitType; }

 public List<Lease> getLeases() { return leases; }
 public void setLeases(List<Lease> leases) { this.leases = leases; }

 /** Overridden by {@link Dorm}; a plain unit has no manager. */
 public String getManagerName() { return null; }
}
