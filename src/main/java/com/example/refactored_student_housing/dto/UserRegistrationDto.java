package com.example.refactored_student_housing.dto;

import jakarta.validation.constraints.*;

public class UserRegistrationDto {

    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Must be a valid email format (e.g., student@example.com)")
    private String email;

    @NotNull(message = "Age is required")
    @Min(value = 16, message = "You must be at least 16 to register")
    @Max(value = 100, message = "Please enter a valid age")
    private Integer age;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}