package com.example.refactored_student_housing.repository;

import com.example.refactored_student_housing.domain.AppUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AppUserRepositoryTest {

    @Autowired
    private AppUserRepository userRepository;

    @Test
    void testUsernameUniqueness_ShouldThrowExceptionForDuplicate() {
        // Arrange
        AppUser user1 = new AppUser();
        user1.setUsername("unique_user");
        user1.setEmail("user1@test.com");
        user1.setPassword("pass");
        user1.setAge(20);
        user1.setRole("USER");
        userRepository.saveAndFlush(user1);

        // Act & Assert
        AppUser user2 = new AppUser();
        user2.setUsername("unique_user"); // Duplicate!
        user2.setEmail("user2@test.com");
        user2.setPassword("pass2");
        user2.setAge(22);
        user2.setRole("USER");

        assertThrows(DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(user2),
                "Should throw exception due to unique username constraint");
    }
}