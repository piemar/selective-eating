package com.example.app.integration;

import com.example.app.BaseIntegrationTest;
import com.example.app.models.Child;
import com.example.app.models.User;
import com.example.app.repositories.ChildRepository;
import com.example.app.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@AutoConfigureWebMvc
class ChildControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private UserRepository userRepository;

    @LocalServerPort
    private int port;

    private User testParent;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1/children";
    }

    @BeforeEach
    void setUpTestData() {
        // Create a test parent user
        testParent = new User("parent@example.com", "Test Parent", "password123", Arrays.asList("USER"));
        testParent = userRepository.save(testParent);
    }

    @Test
    @DisplayName("Should create a new child and save to database")
    void shouldCreateChildAndSaveToDatabase() {
        // Given
        Child newChild = createSampleChild("Alice", LocalDate.of(2018, 5, 15), testParent);
        
        // When
        ResponseEntity<Child> response = restTemplate.postForEntity(getBaseUrl(), newChild, Child.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Alice");
        assertThat(response.getBody().getBirthDate()).isEqualTo(LocalDate.of(2018, 5, 15));
        assertThat(response.getBody().getId()).isNotNull();
        
        // Verify child is saved in database
        List<Child> savedChildren = childRepository.findByParent(testParent);
        assertThat(savedChildren).hasSize(1);
        assertThat(savedChildren.get(0).getName()).isEqualTo("Alice");
        assertThat(savedChildren.get(0).getParent().getId()).isEqualTo(testParent.getId());
    }

    @Test
    @DisplayName("Should retrieve child by ID from database")
    void shouldRetrieveChildByIdFromDatabase() {
        // Given
        Child child = createSampleChild("Bob", LocalDate.of(2019, 3, 20), testParent);
        Child savedChild = childRepository.save(child);
        
        // When
        ResponseEntity<Child> response = restTemplate.getForEntity(
            getBaseUrl() + "/" + savedChild.getId(), Child.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(savedChild.getId());
        assertThat(response.getBody().getName()).isEqualTo("Bob");
        assertThat(response.getBody().getBirthDate()).isEqualTo(LocalDate.of(2019, 3, 20));
    }

    @Test
    @DisplayName("Should return 404 when child not found")
    void shouldReturn404WhenChildNotFound() {
        // When
        ResponseEntity<Child> response = restTemplate.getForEntity(
            getBaseUrl() + "/nonexistent-id", Child.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should update existing child in database")
    void shouldUpdateExistingChildInDatabase() {
        // Given
        Child child = createSampleChild("Charlie", LocalDate.of(2017, 8, 10), testParent);
        child.setTotalFoodsTried(5);
        child.setCurrentStreak(2);
        Child savedChild = childRepository.save(child);
        
        Child updatedChild = createSampleChild("Charlie Updated", LocalDate.of(2017, 8, 10), testParent);
        updatedChild.setTotalFoodsTried(10);
        updatedChild.setCurrentStreak(5);
        updatedChild.setPreferredTextures(Arrays.asList("Soft", "Smooth"));
        updatedChild.setLikedFoodIds(Arrays.asList("food1", "food2"));
        
        HttpEntity<Child> requestEntity = new HttpEntity<>(updatedChild);
        
        // When
        ResponseEntity<Child> response = restTemplate.exchange(
            getBaseUrl() + "/" + savedChild.getId(), 
            HttpMethod.PUT, 
            requestEntity, 
            Child.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(savedChild.getId());
        assertThat(response.getBody().getName()).isEqualTo("Charlie Updated");
        assertThat(response.getBody().getTotalFoodsTried()).isEqualTo(10);
        assertThat(response.getBody().getCurrentStreak()).isEqualTo(5);
        
        // Verify update in database
        Optional<Child> dbChild = childRepository.findById(savedChild.getId());
        assertThat(dbChild).isPresent();
        assertThat(dbChild.get().getName()).isEqualTo("Charlie Updated");
        assertThat(dbChild.get().getPreferredTextures()).containsExactlyInAnyOrder("Soft", "Smooth");
        assertThat(dbChild.get().getLikedFoodIds()).containsExactlyInAnyOrder("food1", "food2");
    }

    @Test
    @DisplayName("Should delete child from database")
    void shouldDeleteChildFromDatabase() {
        // Given
        Child child = createSampleChild("Diana", LocalDate.of(2020, 1, 5), testParent);
        Child savedChild = childRepository.save(child);
        
        // When
        ResponseEntity<Void> response = restTemplate.exchange(
            getBaseUrl() + "/" + savedChild.getId(), 
            HttpMethod.DELETE, 
            null, 
            Void.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        
        // Verify deletion from database
        Optional<Child> deletedChild = childRepository.findById(savedChild.getId());
        assertThat(deletedChild).isEmpty();
    }

    @Test
    @DisplayName("Should retrieve all children from database")
    void shouldRetrieveAllChildrenFromDatabase() {
        // Given
        User secondParent = new User("parent2@example.com", "Second Parent", "password123", Arrays.asList("USER"));
        secondParent = userRepository.save(secondParent);
        
        List<Child> children = Arrays.asList(
            createSampleChild("Child 1", LocalDate.of(2018, 1, 1), testParent),
            createSampleChild("Child 2", LocalDate.of(2019, 2, 2), testParent),
            createSampleChild("Child 3", LocalDate.of(2020, 3, 3), secondParent)
        );
        childRepository.saveAll(children);
        
        // When
        ResponseEntity<Child[]> response = restTemplate.getForEntity(getBaseUrl(), Child[].class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(3);
        
        List<String> childNames = Arrays.stream(response.getBody())
            .map(Child::getName)
            .toList();
        assertThat(childNames).containsExactlyInAnyOrder("Child 1", "Child 2", "Child 3");
    }

    @Test
    @DisplayName("Should test child search by parent")
    void shouldTestChildSearchByParent() {
        // Given
        User secondParent = new User("parent2@example.com", "Second Parent", "password123", Arrays.asList("USER"));
        secondParent = userRepository.save(secondParent);
        
        List<Child> children = Arrays.asList(
            createSampleChild("Parent1 Child1", LocalDate.of(2018, 1, 1), testParent),
            createSampleChild("Parent1 Child2", LocalDate.of(2019, 2, 2), testParent),
            createSampleChild("Parent2 Child1", LocalDate.of(2020, 3, 3), secondParent)
        );
        childRepository.saveAll(children);
        
        // When & Then - Test findByParent
        List<Child> parent1Children = childRepository.findByParent(testParent);
        assertThat(parent1Children).hasSize(2);
        assertThat(parent1Children).extracting("name")
            .containsExactlyInAnyOrder("Parent1 Child1", "Parent1 Child2");
        
        List<Child> parent2Children = childRepository.findByParent(secondParent);
        assertThat(parent2Children).hasSize(1);
        assertThat(parent2Children.get(0).getName()).isEqualTo("Parent2 Child1");
        
        // Test findByParentId
        List<Child> childrenByParentId = childRepository.findByParentId(testParent.getId());
        assertThat(childrenByParentId).hasSize(2);
        
        // Test countByParent
        Long count = childRepository.countByParent(testParent);
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should test child dietary restrictions and allergen queries")
    void shouldTestChildDietaryRestrictionsAndAllergenQueries() {
        // Given
        Child vegetarianChild = createSampleChild("Veg Child", LocalDate.of(2018, 1, 1), testParent);
        vegetarianChild.setDietaryRestrictions(Arrays.asList("vegetarian", "no-spicy"));
        vegetarianChild.setAllergens(Arrays.asList("Nuts"));
        
        Child glutenFreeChild = createSampleChild("GF Child", LocalDate.of(2019, 2, 2), testParent);
        glutenFreeChild.setDietaryRestrictions(Arrays.asList("gluten-free"));
        glutenFreeChild.setAllergens(Arrays.asList("Gluten", "Dairy"));
        
        Child regularChild = createSampleChild("Regular Child", LocalDate.of(2020, 3, 3), testParent);
        regularChild.setDietaryRestrictions(Arrays.asList());
        regularChild.setAllergens(Arrays.asList());
        
        childRepository.saveAll(Arrays.asList(vegetarianChild, glutenFreeChild, regularChild));
        
        // When & Then - Test dietary restriction queries
        List<Child> vegetarians = childRepository.findByDietaryRestrictionsContaining("vegetarian");
        assertThat(vegetarians).hasSize(1);
        assertThat(vegetarians.get(0).getName()).isEqualTo("Veg Child");
        
        List<Child> glutenFreeChildren = childRepository.findByDietaryRestrictionsContaining("gluten-free");
        assertThat(glutenFreeChildren).hasSize(1);
        assertThat(glutenFreeChildren.get(0).getName()).isEqualTo("GF Child");
        
        // Test allergen queries
        List<Child> nutAllergicChildren = childRepository.findByAllergensContaining("Nuts");
        assertThat(nutAllergicChildren).hasSize(1);
        assertThat(nutAllergicChildren.get(0).getName()).isEqualTo("Veg Child");
        
        List<Child> dairyAllergicChildren = childRepository.findByAllergensContaining("Dairy");
        assertThat(dairyAllergicChildren).hasSize(1);
        assertThat(dairyAllergicChildren.get(0).getName()).isEqualTo("GF Child");
    }

    @Test
    @DisplayName("Should test child texture and flavor preference queries")
    void shouldTestChildTextureAndFlavorPreferenceQueries() {
        // Given
        Child softTextureChild = createSampleChild("Soft Lover", LocalDate.of(2018, 1, 1), testParent);
        softTextureChild.setPreferredTextures(Arrays.asList("Soft", "Smooth"));
        softTextureChild.setPreferredFlavors(Arrays.asList("Sweet", "Mild"));
        
        Child crunchyTextureChild = createSampleChild("Crunchy Lover", LocalDate.of(2019, 2, 2), testParent);
        crunchyTextureChild.setPreferredTextures(Arrays.asList("Crunchy", "Hard"));
        crunchyTextureChild.setPreferredFlavors(Arrays.asList("Savory", "Strong"));
        crunchyTextureChild.setAvoidedTextures(Arrays.asList("Slimy"));
        
        childRepository.saveAll(Arrays.asList(softTextureChild, crunchyTextureChild));
        
        // When & Then - Test texture preference queries
        List<Child> softPreferenceChildren = childRepository.findByPreferredTexturesContaining("Soft");
        assertThat(softPreferenceChildren).hasSize(1);
        assertThat(softPreferenceChildren.get(0).getName()).isEqualTo("Soft Lover");
        
        List<Child> crunchyPreferenceChildren = childRepository.findByPreferredTexturesContaining("Crunchy");
        assertThat(crunchyPreferenceChildren).hasSize(1);
        assertThat(crunchyPreferenceChildren.get(0).getName()).isEqualTo("Crunchy Lover");
        
        // Test flavor preference queries
        List<Child> sweetPreferenceChildren = childRepository.findByPreferredFlavorsContaining("Sweet");
        assertThat(sweetPreferenceChildren).hasSize(1);
        assertThat(sweetPreferenceChildren.get(0).getName()).isEqualTo("Soft Lover");
        
        List<Child> savoryPreferenceChildren = childRepository.findByPreferredFlavorsContaining("Savory");
        assertThat(savoryPreferenceChildren).hasSize(1);
        assertThat(savoryPreferenceChildren.get(0).getName()).isEqualTo("Crunchy Lover");
    }

    @Test
    @DisplayName("Should test child progress tracking queries")
    void shouldTestChildProgressTrackingQueries() {
        // Given
        Child activeChild = createSampleChild("Active Child", LocalDate.of(2018, 1, 1), testParent);
        activeChild.setTotalFoodsTried(15);
        activeChild.setNewFavoritesCount(3);
        activeChild.setCurrentStreak(5);
        activeChild.setExplorationProgress(75.0);
        
        Child beginnerChild = createSampleChild("Beginner Child", LocalDate.of(2019, 2, 2), testParent);
        beginnerChild.setTotalFoodsTried(2);
        beginnerChild.setNewFavoritesCount(1);
        beginnerChild.setCurrentStreak(0);
        beginnerChild.setExplorationProgress(10.0);
        
        Child advancedChild = createSampleChild("Advanced Child", LocalDate.of(2020, 3, 3), testParent);
        advancedChild.setTotalFoodsTried(25);
        advancedChild.setNewFavoritesCount(8);
        advancedChild.setCurrentStreak(10);
        advancedChild.setExplorationProgress(90.0);
        
        childRepository.saveAll(Arrays.asList(activeChild, beginnerChild, advancedChild));
        
        // When & Then - Test progress queries
        List<Child> activeChildren = childRepository.findActiveChildren();
        assertThat(activeChildren).hasSize(2); // Those with currentStreak > 0
        assertThat(activeChildren).extracting("name")
            .containsExactlyInAnyOrder("Active Child", "Advanced Child");
        
        List<Child> experiencedChildren = childRepository.findByTotalFoodsTriedGreaterThanEqual(10);
        assertThat(experiencedChildren).hasSize(2);
        assertThat(experiencedChildren).extracting("name")
            .containsExactlyInAnyOrder("Active Child", "Advanced Child");
        
        List<Child> highFavoriteChildren = childRepository.findByNewFavoritesCountGreaterThanEqual(3);
        assertThat(highFavoriteChildren).hasSize(2);
        assertThat(highFavoriteChildren).extracting("name")
            .containsExactlyInAnyOrder("Active Child", "Advanced Child");
        
        List<Child> highProgressChildren = childRepository.findByExplorationProgressGreaterThanEqual(50.0);
        assertThat(highProgressChildren).hasSize(2);
        assertThat(highProgressChildren).extracting("name")
            .containsExactlyInAnyOrder("Active Child", "Advanced Child");
    }

    @Test
    @DisplayName("Should test child food preference tracking")
    void shouldTestChildFoodPreferenceTracking() {
        // Given
        Child child1 = createSampleChild("Child 1", LocalDate.of(2018, 1, 1), testParent);
        child1.setLikedFoodIds(Arrays.asList("food1", "food2", "food3"));
        child1.setDislikedFoodIds(Arrays.asList("food4", "food5"));
        
        Child child2 = createSampleChild("Child 2", LocalDate.of(2019, 2, 2), testParent);
        child2.setLikedFoodIds(Arrays.asList("food2", "food6"));
        child2.setDislikedFoodIds(Arrays.asList("food1", "food7"));
        
        childRepository.saveAll(Arrays.asList(child1, child2));
        
        // When & Then - Test food preference queries
        List<Child> likesFood1 = childRepository.findByLikedFoodIdsContaining("food1");
        assertThat(likesFood1).hasSize(1);
        assertThat(likesFood1.get(0).getName()).isEqualTo("Child 1");
        
        List<Child> likesFood2 = childRepository.findByLikedFoodIdsContaining("food2");
        assertThat(likesFood2).hasSize(2);
        assertThat(likesFood2).extracting("name").containsExactlyInAnyOrder("Child 1", "Child 2");
        
        List<Child> dislikesFood1 = childRepository.findByDislikedFoodIdsContaining("food1");
        assertThat(dislikesFood1).hasSize(1);
        assertThat(dislikesFood1.get(0).getName()).isEqualTo("Child 2");
    }

    @Test
    @DisplayName("Should test child name uniqueness per parent")
    void shouldTestChildNameUniquenessPerParent() {
        // Given
        User secondParent = new User("parent2@example.com", "Second Parent", "password123", Arrays.asList("USER"));
        secondParent = userRepository.save(secondParent);
        
        Child child1 = createSampleChild("John", LocalDate.of(2018, 1, 1), testParent);
        Child child2 = createSampleChild("John", LocalDate.of(2019, 2, 2), secondParent); // Same name, different parent
        
        childRepository.saveAll(Arrays.asList(child1, child2));
        
        // When & Then - Test findByNameAndParent
        Optional<Child> johnFromParent1 = childRepository.findByNameAndParent("John", testParent);
        assertThat(johnFromParent1).isPresent();
        assertThat(johnFromParent1.get().getBirthDate()).isEqualTo(LocalDate.of(2018, 1, 1));
        
        Optional<Child> johnFromParent2 = childRepository.findByNameAndParent("John", secondParent);
        assertThat(johnFromParent2).isPresent();
        assertThat(johnFromParent2.get().getBirthDate()).isEqualTo(LocalDate.of(2019, 2, 2));
        
        // Different names should not be found
        Optional<Child> nonExistent = childRepository.findByNameAndParent("Jane", testParent);
        assertThat(nonExistent).isEmpty();
    }

    private Child createSampleChild(String name, LocalDate birthDate, User parent) {
        Child child = new Child(name, birthDate, parent);
        child.setTotalFoodsTried(0);
        child.setNewFavoritesCount(0);
        child.setCurrentStreak(0);
        child.setLongestStreak(0);
        child.setExplorationProgress(0.0);
        return child;
    }
}
