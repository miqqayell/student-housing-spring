package com.example.refactored_student_housing.service;

import com.example.refactored_student_housing.domain.AppUser;
import com.example.refactored_student_housing.dto.UserRegistrationDto;
import com.example.refactored_student_housing.exception.ServiceException;
import com.example.refactored_student_housing.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registration used to live in the controller, including the password hashing
 * and the role assignment. Those are business rules, so they belong here.
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    /** Self-registered accounts never get the administrator role. */
    private static final String DEFAULT_ROLE = "USER";

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUsernameTaken(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Override
    public void register(UserRegistrationDto dto) {
        if (isUsernameTaken(dto.getUsername())) {
            throw new ServiceException("That username is already taken.");
        }

        AppUser user = new AppUser();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setAge(dto.getAge());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(DEFAULT_ROLE);

        userRepository.save(user);
    }
}
