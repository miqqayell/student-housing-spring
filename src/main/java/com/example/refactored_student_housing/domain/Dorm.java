    package com.example.refactored_student_housing.domain;

    import jakarta.persistence.DiscriminatorValue;
    import jakarta.persistence.Entity;

    @Entity
    @DiscriminatorValue("DORM")
    public class Dorm extends HousingUnit {

        private String managerName;

        @Override
        public String getManagerName() { return managerName; }
        public void setManagerName(String managerName) { this.managerName = managerName; }
    }