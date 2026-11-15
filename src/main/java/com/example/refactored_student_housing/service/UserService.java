package com.example.refactored_student_housing.service;

import com.example.refactored_student_housing.dto.UserRegistrationDto;

public interface UserService {

    boolean isUsernameTaken(String username);

    /** Creates a new account with the default role and a hashed password. */
    void register(UserRegistrationDto dto);
}
