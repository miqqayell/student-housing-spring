package com.example.refactored_student_housing.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students")
public class Student {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @Column(nullable = false)
 private String name;

 private LocalDate birthDate;
 private int yearOfStudy;
 private Double gpa;
 private boolean international;
 private String profileImageUrl;

 // Cascade is enabled here on purpose: a Lease has no meaning without the
 // Student it belongs to, so removing a student must remove their leases
 // (orphanRemoval) instead of leaving rows pointing at a deleted student.
 // Fetching stays LAZY - the leases are loaded explicitly with a JOIN FETCH
 // by the queries that actually need them.
 @OneToMany(mappedBy = "student", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
 private List<Lease> leases = new ArrayList<>();

 // Week 5: the user who created (and therefore owns) this record.
 // Explicitly LAZY - eager loading is disabled throughout the project; the
 // advisor is join-fetched by the queries that need it.
 @ManyToOne(fetch = FetchType.LAZY)
 @JoinColumn(name = "advisor_id")
 private AppUser advisor;

 public Long getId() { return id; }
 public void setId(Long id) { this.id = id; }

 public String getName() { return name; }
 public void setName(String name) { this.name = name; }

 public LocalDate getBirthDate() { return birthDate; }
 public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

 public int getYearOfStudy() { return yearOfStudy; }
 public void setYearOfStudy(int yearOfStudy) { this.yearOfStudy = yearOfStudy; }

 public Double getGpa() { return gpa; }
 public void setGpa(Double gpa) { this.gpa = gpa; }

 public boolean isInternational() { return international; }
 public void setInternational(boolean international) { this.international = international; }

 public String getProfileImageUrl() { return profileImageUrl; }
 public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

 public List<Lease> getLeases() { return leases; }
 public void setLeases(List<Lease> leases) { this.leases = leases; }

 public AppUser getAdvisor() { return advisor; }
 public void setAdvisor(AppUser advisor) { this.advisor = advisor; }

 /** Keeps both sides of the association consistent when a lease is added. */
 public void addLease(Lease lease) {
  leases.add(lease);
  lease.setStudent(this);
 }
}
