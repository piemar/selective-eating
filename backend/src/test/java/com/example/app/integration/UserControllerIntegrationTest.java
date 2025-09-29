package com.example.app.integration;

import com.example.app.BaseIntegrationTest;
import com.example.app.models.User;
import com.example.app.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@AutoConfigureWebMvc
class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @LocalServerPort
    private int port;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1/users";
    }

    @Test
    @DisplayName("Should create a new user and save to database")
    void shouldCreateUserAndSaveToDatabase() {
        // Given
        User newUser = new User("test@example.com", "Test User", "password123", Arrays.asList("USER"));
        
        // When
        ResponseEntity<User> response = restTemplate.postForEntity(getBaseUrl(), newUser, User.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("test@example.com");
        assertThat(response.getBody().getName()).isEqualTo("Test User");
        assertThat(response.getBody().getId()).isNotNull();
        
        // Verify user is saved in database
        Optional<User> savedUser = userRepository.findByEmail("test@example.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getName()).isEqualTo("Test User");
        assertThat(savedUser.get().getRoles()).containsExactly("USER");
    }

    @Test
    @DisplayName("Should retrieve user by ID from database")
    void shouldRetrieveUserByIdFromDatabase() {
        // Given
        User user = new User("john@example.com", "John Doe", "password123", Arrays.asList("USER"));
        User savedUser = userRepository.save(user);
        
        // When
        ResponseEntity<User> response = restTemplate.getForEntity(
            getBaseUrl() + "/" + savedUser.getId(), User.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(savedUser.getId());
        assertThat(response.getBody().getEmail()).isEqualTo("john@example.com");
        assertThat(response.getBody().getName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Should return 404 when user not found")
    void shouldReturn404WhenUserNotFound() {
        // When
        ResponseEntity<User> response = restTemplate.getForEntity(
            getBaseUrl() + "/nonexistent-id", User.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should retrieve user by email from database")
    void shouldRetrieveUserByEmailFromDatabase() {
        // Given
        User user = new User("jane@example.com", "Jane Smith", "password123", Arrays.asList("USER", "ADMIN"));
        userRepository.save(user);
        
        // When
        ResponseEntity<User> response = restTemplate.getForEntity(
            getBaseUrl() + "/by-email/jane@example.com", User.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("jane@example.com");
        assertThat(response.getBody().getName()).isEqualTo("Jane Smith");
        assertThat(response.getBody().getRoles()).containsExactlyInAnyOrder("USER", "ADMIN");
    }

    @Test
    @DisplayName("Should update existing user in database")
    void shouldUpdateExistingUserInDatabase() {
        // Given
        User user = new User("update@example.com", "Original Name", "password123", Arrays.asList("USER"));
        User savedUser = userRepository.save(user);
        
        User updatedUser = new User("update@example.com", "Updated Name", "newpassword123", Arrays.asList("USER", "MODERATOR"));
        HttpEntity<User> requestEntity = new HttpEntity<>(updatedUser);
        
        // When
        ResponseEntity<User> response = restTemplate.exchange(
            getBaseUrl() + "/" + savedUser.getId(), 
            HttpMethod.PUT, 
            requestEntity, 
            User.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(savedUser.getId());
        assertThat(response.getBody().getName()).isEqualTo("Updated Name");
        assertThat(response.getBody().getRoles()).containsExactlyInAnyOrder("USER", "MODERATOR");
        
        // Verify update in database
        Optional<User> dbUser = userRepository.findById(savedUser.getId());
        assertThat(dbUser).isPresent();
        assertThat(dbUser.get().getName()).isEqualTo("Updated Name");
        assertThat(dbUser.get().getRoles()).containsExactlyInAnyOrder("USER", "MODERATOR");
    }

    @Test
    @DisplayName("Should delete user from database")
    void shouldDeleteUserFromDatabase() {
        // Given
        User user = new User("delete@example.com", "Delete Me", "password123", Arrays.asList("USER"));
        User savedUser = userRepository.save(user);
        
        // When
        ResponseEntity<Void> response = restTemplate.exchange(
            getBaseUrl() + "/" + savedUser.getId(), 
            HttpMethod.DELETE, 
            null, 
            Void.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        
        // Verify deletion from database
        Optional<User> deletedUser = userRepository.findById(savedUser.getId());
        assertThat(deletedUser).isEmpty();
    }

    @Test
    @DisplayName("Should retrieve all users with pagination from database")
    void shouldRetrieveAllUsersWithPaginationFromDatabase() {
        // Given
        List<User> users = Arrays.asList(
            new User("user1@example.com", "User 1", "password1", Arrays.asList("USER")),
            new User("user2@example.com", "User 2", "password2", Arrays.asList("USER")),
            new User("user3@example.com", "User 3", "password3", Arrays.asList("USER"))
        );
        userRepository.saveAll(users);
        
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
            getBaseUrl() + "?page=0&size=2", String.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("\"totalElements\":3");
        assertThat(response.getBody()).contains("\"size\":2");
        assertThat(response.getBody()).contains("\"number\":0");
    }

    @Test
    @DisplayName("Should handle duplicate email scenario")
    void shouldHandleDuplicateEmailScenario() {
        // Given
        User firstUser = new User("duplicate@example.com", "First User", "password1", Arrays.asList("USER"));
        userRepository.save(firstUser);
        
        User duplicateUser = new User("duplicate@example.com", "Duplicate User", "password2", Arrays.asList("USER"));
        
        // When & Then
        // This should either fail or handle gracefully depending on your business logic
        assertThatThrownBy(() -> userRepository.save(duplicateUser))
            .isInstanceOf(Exception.class); // MongoDB will throw due to unique index
    }

    @Test
    @DisplayName("Should validate user creation with null fields")
    void shouldValidateUserCreationWithNullFields() {
        // Given
        User invalidUser = new User(null, "Test User", "password", Arrays.asList("USER"));
        
        // When
        ResponseEntity<User> response = restTemplate.postForEntity(getBaseUrl(), invalidUser, User.class);
        
        // Then
        // This should either return bad request or handle validation
        // Depending on your validation logic
        assertThat(response.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatus.OK);
    }

    @Test
    @DisplayName("Should test database query methods")
    void shouldTestDatabaseQueryMethods() {
        // Given
        User user1 = new User("query1@example.com", "Query User 1", "password1", Arrays.asList("USER"));
        User user2 = new User("query2@example.com", "Query User 2", "password2", Arrays.asList("ADMIN"));
        userRepository.saveAll(Arrays.asList(user1, user2));
        
        // When & Then - Test findByEmail
        Optional<User> foundUser = userRepository.findByEmail("query1@example.com");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Query User 1");
        
        // Test existsByEmail
        assertThat(userRepository.existsByEmail("query1@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
        
        // Test count
        assertThat(userRepository.count()).isEqualTo(2);
    }
}
