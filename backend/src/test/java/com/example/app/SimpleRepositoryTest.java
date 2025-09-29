package com.example.app;

import com.example.app.models.User;
import com.example.app.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simple repository test that doesn't require Docker.
 * Uses Spring Boot's @DataMongoTest for lightweight database testing.
 * 
 * This test demonstrates that the repository layer works correctly
 * without requiring full application context or TestContainers.
 */
@DataMongoTest
@ActiveProfiles("test")
class SimpleRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should save and find user by email")
    void shouldSaveAndFindUserByEmail() {
        // Given
        User user = new User("test@example.com", "Test User", "password123", Arrays.asList("USER"));

        // When
        User savedUser = userRepository.save(user);
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Test User");
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.get().getRoles()).containsExactly("USER");
    }

    @Test
    @DisplayName("Should check if email exists")
    void shouldCheckIfEmailExists() {
        // Given
        User user = new User("exists@example.com", "Existing User", "password123", Arrays.asList("USER"));
        userRepository.save(user);

        // When & Then
        assertThat(userRepository.existsByEmail("exists@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("notexists@example.com")).isFalse();
    }

    @Test
    @DisplayName("Should handle basic CRUD operations")
    void shouldHandleBasicCrudOperations() {
        // Create
        User user = new User("crud@example.com", "CRUD Test", "password123", Arrays.asList("USER"));
        User saved = userRepository.save(user);
        assertThat(saved.getId()).isNotNull();

        // Read
        Optional<User> found = userRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("CRUD Test");

        // Update
        found.get().setName("Updated CRUD Test");
        User updated = userRepository.save(found.get());
        assertThat(updated.getName()).isEqualTo("Updated CRUD Test");

        // Delete
        userRepository.deleteById(saved.getId());
        Optional<User> deleted = userRepository.findById(saved.getId());
        assertThat(deleted).isEmpty();
    }
}
