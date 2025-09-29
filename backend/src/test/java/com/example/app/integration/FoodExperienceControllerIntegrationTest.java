package com.example.app.integration;

import com.example.app.BaseIntegrationTest;
import com.example.app.models.Child;
import com.example.app.models.Food;
import com.example.app.models.FoodExperience;
import com.example.app.models.User;
import com.example.app.repositories.ChildRepository;
import com.example.app.repositories.FoodExperienceRepository;
import com.example.app.repositories.FoodRepository;
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

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@AutoConfigureWebMvc
class FoodExperienceControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FoodExperienceRepository foodExperienceRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private UserRepository userRepository;

    @LocalServerPort
    private int port;

    private User testParent;
    private Child testChild;
    private Food testFood1;
    private Food testFood2;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1/food-experiences";
    }

    @BeforeEach
    void setUpTestData() {
        // Create test parent
        testParent = new User("parent@example.com", "Test Parent", "password123", Arrays.asList("USER"));
        testParent = userRepository.save(testParent);

        // Create test child
        testChild = new Child("Test Child", LocalDate.of(2018, 5, 15), testParent);
        testChild = childRepository.save(testChild);

        // Create test foods
        testFood1 = new Food("Apple", "Red apple", "apple.jpg", Arrays.asList("Sweet", "Crunchy"));
        testFood1.setCategories(Arrays.asList("Fruit"));
        testFood1 = foodRepository.save(testFood1);

        testFood2 = new Food("Broccoli", "Green vegetable", "broccoli.jpg", Arrays.asList("Healthy", "Green"));
        testFood2.setCategories(Arrays.asList("Vegetable"));
        testFood2 = foodRepository.save(testFood2);
    }

    @Test
    @DisplayName("Should create a new food experience and save to database")
    void shouldCreateFoodExperienceAndSaveToDatabase() {
        // Given
        FoodExperience newExperience = createSampleFoodExperience(testChild, testFood1, 4, "Really enjoyed it!");
        
        // When
        ResponseEntity<FoodExperience> response = restTemplate.postForEntity(getBaseUrl(), newExperience, FoodExperience.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getRating()).isEqualTo(4);
        assertThat(response.getBody().getNotes()).isEqualTo("Really enjoyed it!");
        assertThat(response.getBody().getReaction()).isEqualTo("liked");
        assertThat(response.getBody().getId()).isNotNull();
        
        // Verify experience is saved in database
        List<FoodExperience> savedExperiences = foodExperienceRepository.findByChild(testChild);
        assertThat(savedExperiences).hasSize(1);
        assertThat(savedExperiences.get(0).getRating()).isEqualTo(4);
        assertThat(savedExperiences.get(0).getFood().getName()).isEqualTo("Apple");
    }

    @Test
    @DisplayName("Should retrieve food experience by ID from database")
    void shouldRetrieveFoodExperienceByIdFromDatabase() {
        // Given
        FoodExperience experience = createSampleFoodExperience(testChild, testFood2, 2, "Not a fan");
        FoodExperience savedExperience = foodExperienceRepository.save(experience);
        
        // When
        ResponseEntity<FoodExperience> response = restTemplate.getForEntity(
            getBaseUrl() + "/" + savedExperience.getId(), FoodExperience.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(savedExperience.getId());
        assertThat(response.getBody().getRating()).isEqualTo(2);
        assertThat(response.getBody().getNotes()).isEqualTo("Not a fan");
        assertThat(response.getBody().getReaction()).isEqualTo("disliked");
    }

    @Test
    @DisplayName("Should return 404 when food experience not found")
    void shouldReturn404WhenFoodExperienceNotFound() {
        // When
        ResponseEntity<FoodExperience> response = restTemplate.getForEntity(
            getBaseUrl() + "/nonexistent-id", FoodExperience.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should update existing food experience in database")
    void shouldUpdateExistingFoodExperienceInDatabase() {
        // Given
        FoodExperience experience = createSampleFoodExperience(testChild, testFood1, 3, "It was okay");
        experience.setWasFirstTime(true);
        experience.setMealType("lunch");
        FoodExperience savedExperience = foodExperienceRepository.save(experience);
        
        FoodExperience updatedExperience = createSampleFoodExperience(testChild, testFood1, 5, "Actually love it now!");
        updatedExperience.setWasFirstTime(true); // Keep this
        updatedExperience.setMealType("dinner");
        updatedExperience.setWillTryAgain(true);
        updatedExperience.setEnvironment("home");
        
        HttpEntity<FoodExperience> requestEntity = new HttpEntity<>(updatedExperience);
        
        // When
        ResponseEntity<FoodExperience> response = restTemplate.exchange(
            getBaseUrl() + "/" + savedExperience.getId(), 
            HttpMethod.PUT, 
            requestEntity, 
            FoodExperience.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(savedExperience.getId());
        assertThat(response.getBody().getRating()).isEqualTo(5);
        assertThat(response.getBody().getNotes()).isEqualTo("Actually love it now!");
        assertThat(response.getBody().getReaction()).isEqualTo("liked");
        
        // Verify update in database
        Optional<FoodExperience> dbExperience = foodExperienceRepository.findById(savedExperience.getId());
        assertThat(dbExperience).isPresent();
        assertThat(dbExperience.get().getMealType()).isEqualTo("dinner");
        assertThat(dbExperience.get().getWillTryAgain()).isTrue();
        assertThat(dbExperience.get().getEnvironment()).isEqualTo("home");
    }

    @Test
    @DisplayName("Should delete food experience from database")
    void shouldDeleteFoodExperienceFromDatabase() {
        // Given
        FoodExperience experience = createSampleFoodExperience(testChild, testFood1, 3, "Meh");
        FoodExperience savedExperience = foodExperienceRepository.save(experience);
        
        // When
        ResponseEntity<Void> response = restTemplate.exchange(
            getBaseUrl() + "/" + savedExperience.getId(), 
            HttpMethod.DELETE, 
            null, 
            Void.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        
        // Verify deletion from database
        Optional<FoodExperience> deletedExperience = foodExperienceRepository.findById(savedExperience.getId());
        assertThat(deletedExperience).isEmpty();
    }

    @Test
    @DisplayName("Should retrieve all food experiences from database")
    void shouldRetrieveAllFoodExperiencesFromDatabase() {
        // Given
        Child secondChild = new Child("Second Child", LocalDate.of(2019, 3, 20), testParent);
        secondChild = childRepository.save(secondChild);
        
        List<FoodExperience> experiences = Arrays.asList(
            createSampleFoodExperience(testChild, testFood1, 4, "Good"),
            createSampleFoodExperience(testChild, testFood2, 2, "Bad"),
            createSampleFoodExperience(secondChild, testFood1, 5, "Excellent")
        );
        foodExperienceRepository.saveAll(experiences);
        
        // When
        ResponseEntity<FoodExperience[]> response = restTemplate.getForEntity(getBaseUrl(), FoodExperience[].class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(3);
        
        List<Integer> ratings = Arrays.stream(response.getBody())
            .map(FoodExperience::getRating)
            .toList();
        assertThat(ratings).containsExactlyInAnyOrder(4, 2, 5);
    }

    @Test
    @DisplayName("Should test food experience queries by child")
    void shouldTestFoodExperienceQueriesByChild() {
        // Given
        Child secondChild = new Child("Second Child", LocalDate.of(2019, 3, 20), testParent);
        secondChild = childRepository.save(secondChild);
        
        List<FoodExperience> experiences = Arrays.asList(
            createSampleFoodExperience(testChild, testFood1, 4, "Child1 Apple"),
            createSampleFoodExperience(testChild, testFood2, 3, "Child1 Broccoli"),
            createSampleFoodExperience(secondChild, testFood1, 5, "Child2 Apple"),
            createSampleFoodExperience(secondChild, testFood2, 2, "Child2 Broccoli")
        );
        foodExperienceRepository.saveAll(experiences);
        
        // When & Then - Test findByChild
        List<FoodExperience> child1Experiences = foodExperienceRepository.findByChild(testChild);
        assertThat(child1Experiences).hasSize(2);
        assertThat(child1Experiences).extracting("notes")
            .containsExactlyInAnyOrder("Child1 Apple", "Child1 Broccoli");
        
        List<FoodExperience> child2Experiences = foodExperienceRepository.findByChild(secondChild);
        assertThat(child2Experiences).hasSize(2);
        assertThat(child2Experiences).extracting("notes")
            .containsExactlyInAnyOrder("Child2 Apple", "Child2 Broccoli");
        
        // Test findByChildId
        List<FoodExperience> child1ExperiencesById = foodExperienceRepository.findByChildId(testChild.getId());
        assertThat(child1ExperiencesById).hasSize(2);
    }

    @Test
    @DisplayName("Should test food experience queries by food")
    void shouldTestFoodExperienceQueriesByFood() {
        // Given
        Child secondChild = new Child("Second Child", LocalDate.of(2019, 3, 20), testParent);
        secondChild = childRepository.save(secondChild);
        
        List<FoodExperience> experiences = Arrays.asList(
            createSampleFoodExperience(testChild, testFood1, 4, "Child1 likes apple"),
            createSampleFoodExperience(secondChild, testFood1, 2, "Child2 dislikes apple"),
            createSampleFoodExperience(testChild, testFood2, 3, "Child1 neutral broccoli")
        );
        foodExperienceRepository.saveAll(experiences);
        
        // When & Then - Test findByFood
        List<FoodExperience> appleExperiences = foodExperienceRepository.findByFood(testFood1);
        assertThat(appleExperiences).hasSize(2);
        assertThat(appleExperiences).extracting("rating").containsExactlyInAnyOrder(4, 2);
        
        List<FoodExperience> broccoliExperiences = foodExperienceRepository.findByFood(testFood2);
        assertThat(broccoliExperiences).hasSize(1);
        assertThat(broccoliExperiences.get(0).getRating()).isEqualTo(3);
        
        // Test findByFoodId
        List<FoodExperience> appleExperiencesById = foodExperienceRepository.findByFoodId(testFood1.getId());
        assertThat(appleExperiencesById).hasSize(2);
        
        // Test findByChildAndFood
        Optional<FoodExperience> specificExperience = foodExperienceRepository.findByChildAndFood(testChild, testFood1);
        assertThat(specificExperience).isPresent();
        assertThat(specificExperience.get().getRating()).isEqualTo(4);
    }

    @Test
    @DisplayName("Should test food experience queries by rating")
    void shouldTestFoodExperienceQueriesByRating() {
        // Given
        List<FoodExperience> experiences = Arrays.asList(
            createSampleFoodExperience(testChild, testFood1, 5, "Love it"),
            createSampleFoodExperience(testChild, testFood2, 2, "Don't like"),
            createSampleFoodExperience(testChild, testFood1, 4, "Pretty good")
        );
        experiences.get(2).setFood(createAndSaveFood("Orange", "Citrus fruit"));
        foodExperienceRepository.saveAll(experiences);
        
        // When & Then - Test rating queries
        List<FoodExperience> highRated = foodExperienceRepository.findByRating(5);
        assertThat(highRated).hasSize(1);
        assertThat(highRated.get(0).getNotes()).isEqualTo("Love it");
        
        List<FoodExperience> childHighRated = foodExperienceRepository.findByChildAndRating(testChild, 5);
        assertThat(childHighRated).hasSize(1);
        
        List<FoodExperience> childGoodOrBetter = foodExperienceRepository.findByChildAndRatingGreaterThanEqual(testChild, 4);
        assertThat(childGoodOrBetter).hasSize(2);
        assertThat(childGoodOrBetter).extracting("rating").containsExactlyInAnyOrder(5, 4);
    }

    @Test
    @DisplayName("Should test food experience queries by reaction")
    void shouldTestFoodExperienceQueriesByReaction() {
        // Given
        List<FoodExperience> experiences = Arrays.asList(
            createSampleFoodExperience(testChild, testFood1, 5, "Loved"), // liked
            createSampleFoodExperience(testChild, testFood2, 2, "Hated"), // disliked
            createSampleFoodExperience(testChild, createAndSaveFood("Banana", "Yellow fruit"), 3, "Okay") // neutral
        );
        foodExperienceRepository.saveAll(experiences);
        
        // When & Then - Test reaction queries
        List<FoodExperience> likedExperiences = foodExperienceRepository.findByReaction("liked");
        assertThat(likedExperiences).hasSize(1);
        assertThat(likedExperiences.get(0).getNotes()).isEqualTo("Loved");
        
        List<FoodExperience> dislikedExperiences = foodExperienceRepository.findByReaction("disliked");
        assertThat(dislikedExperiences).hasSize(1);
        assertThat(dislikedExperiences.get(0).getNotes()).isEqualTo("Hated");
        
        List<FoodExperience> childLikedExperiences = foodExperienceRepository.findByChildAndReaction(testChild, "liked");
        assertThat(childLikedExperiences).hasSize(1);
        assertThat(childLikedExperiences.get(0).getRating()).isEqualTo(5);
        
        List<FoodExperience> childDislikedExperiences = foodExperienceRepository.findByChildAndReaction(testChild, "disliked");
        assertThat(childDislikedExperiences).hasSize(1);
        assertThat(childDislikedExperiences.get(0).getRating()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should test food experience time-based queries")
    void shouldTestFoodExperienceTimeBasedQueries() {
        // Given
        Instant twoDaysAgo = Instant.now().minus(2, ChronoUnit.DAYS);
        Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant now = Instant.now();
        
        FoodExperience oldExperience = createSampleFoodExperience(testChild, testFood1, 4, "Old experience");
        oldExperience.setCreatedAt(twoDaysAgo);
        
        FoodExperience recentExperience = createSampleFoodExperience(testChild, testFood2, 5, "Recent experience");
        recentExperience.setCreatedAt(yesterday);
        
        FoodExperience todayExperience = createSampleFoodExperience(testChild, createAndSaveFood("Banana", "Yellow"), 3, "Today experience");
        todayExperience.setCreatedAt(now);
        
        foodExperienceRepository.saveAll(Arrays.asList(oldExperience, recentExperience, todayExperience));
        
        // When & Then - Test time-based queries
        List<FoodExperience> recentExperiences = foodExperienceRepository.findByChildAndCreatedAtAfter(
            testChild, yesterday.minus(1, ChronoUnit.HOURS));
        assertThat(recentExperiences).hasSizeGreaterThanOrEqualTo(2);
        
        List<FoodExperience> allRecentExperiences = foodExperienceRepository.findByCreatedAtAfter(twoDaysAgo);
        assertThat(allRecentExperiences).hasSize(3);
        
        // Test date range queries
        List<FoodExperience> rangeExperiences = foodExperienceRepository.findByChildAndCreatedAtBetween(
            testChild, twoDaysAgo.minus(1, ChronoUnit.HOURS), yesterday.plus(1, ChronoUnit.HOURS));
        assertThat(rangeExperiences).hasSize(2);
    }

    @Test
    @DisplayName("Should test first-time experience tracking")
    void shouldTestFirstTimeExperienceTracking() {
        // Given
        FoodExperience firstTime = createSampleFoodExperience(testChild, testFood1, 4, "First time trying");
        firstTime.setWasFirstTime(true);
        
        FoodExperience notFirstTime = createSampleFoodExperience(testChild, testFood2, 3, "Had before");
        notFirstTime.setWasFirstTime(false);
        
        FoodExperience anotherFirstTime = createSampleFoodExperience(testChild, createAndSaveFood("Mango", "Tropical"), 5, "First mango");
        anotherFirstTime.setWasFirstTime(true);
        
        foodExperienceRepository.saveAll(Arrays.asList(firstTime, notFirstTime, anotherFirstTime));
        
        // When & Then - Test first time queries
        List<FoodExperience> firstTimeExperiences = foodExperienceRepository.findByChildAndWasFirstTime(testChild, true);
        assertThat(firstTimeExperiences).hasSize(2);
        assertThat(firstTimeExperiences).extracting("notes")
            .containsExactlyInAnyOrder("First time trying", "First mango");
        
        List<FoodExperience> notFirstTimeExperiences = foodExperienceRepository.findByChildAndWasFirstTime(testChild, false);
        assertThat(notFirstTimeExperiences).hasSize(1);
        assertThat(notFirstTimeExperiences.get(0).getNotes()).isEqualTo("Had before");
    }

    @Test
    @DisplayName("Should test meal type and environment context")
    void shouldTestMealTypeAndEnvironmentContext() {
        // Given
        FoodExperience breakfast = createSampleFoodExperience(testChild, testFood1, 4, "Morning apple");
        breakfast.setMealType("breakfast");
        breakfast.setEnvironment("home");
        
        FoodExperience lunch = createSampleFoodExperience(testChild, testFood2, 3, "School lunch");
        lunch.setMealType("lunch");
        lunch.setEnvironment("school");
        lunch.setMood("reluctant");
        
        FoodExperience dinner = createSampleFoodExperience(testChild, createAndSaveFood("Pasta", "Italian"), 5, "Family dinner");
        dinner.setMealType("dinner");
        dinner.setEnvironment("home");
        dinner.setMood("happy");
        
        foodExperienceRepository.saveAll(Arrays.asList(breakfast, lunch, dinner));
        
        // When & Then - Test context queries (using child-specific methods)
        List<FoodExperience> breakfastExperiences = foodExperienceRepository.findByChildAndMealType(testChild, "breakfast");
        assertThat(breakfastExperiences).hasSize(1);
        assertThat(breakfastExperiences.get(0).getNotes()).isEqualTo("Morning apple");
        
        List<FoodExperience> homeExperiences = foodExperienceRepository.findByChildAndEnvironment(testChild, "home");
        assertThat(homeExperiences).hasSize(2);
        assertThat(homeExperiences).extracting("notes")
            .containsExactlyInAnyOrder("Morning apple", "Family dinner");
        
        // Note: findByMood method doesn't exist in repository, so we'll test manually
        List<FoodExperience> allExperiences = foodExperienceRepository.findByChild(testChild);
        List<FoodExperience> happyExperiences = allExperiences.stream()
            .filter(exp -> "happy".equals(exp.getMood()))
            .toList();
        assertThat(happyExperiences).hasSize(1);
        assertThat(happyExperiences.get(0).getNotes()).isEqualTo("Family dinner");
    }

    @Test
    @DisplayName("Should test food experience data integrity")
    void shouldTestFoodExperienceDataIntegrity() {
        // Given
        FoodExperience experience = createSampleFoodExperience(testChild, testFood1, 4, "Test experience");
        experience.setPortion("small");
        experience.setPreparationMethod("raw");
        experience.setAccompaniedBy("yogurt");
        experience.setWillTryAgain(true);
        
        FoodExperience savedExperience = foodExperienceRepository.save(experience);
        
        // When - Retrieve the experience
        Optional<FoodExperience> retrievedExperience = foodExperienceRepository.findById(savedExperience.getId());
        
        // Then - Verify all fields are preserved
        assertThat(retrievedExperience).isPresent();
        FoodExperience exp = retrievedExperience.get();
        
        assertThat(exp.getChild().getId()).isEqualTo(testChild.getId());
        assertThat(exp.getFood().getId()).isEqualTo(testFood1.getId());
        assertThat(exp.getRating()).isEqualTo(4);
        assertThat(exp.getNotes()).isEqualTo("Test experience");
        assertThat(exp.getReaction()).isEqualTo("liked");
        assertThat(exp.getPortion()).isEqualTo("small");
        assertThat(exp.getPreparationMethod()).isEqualTo("raw");
        assertThat(exp.getAccompaniedBy()).isEqualTo("yogurt");
        assertThat(exp.getWillTryAgain()).isTrue();
        assertThat(exp.getCreatedAt()).isNotNull();
        assertThat(exp.getUpdatedAt()).isNotNull();
    }

    private FoodExperience createSampleFoodExperience(Child child, Food food, Integer rating, String notes) {
        return new FoodExperience(child, food, rating, notes);
    }

    private Food createAndSaveFood(String name, String description) {
        Food food = new Food(name, description, name.toLowerCase() + ".jpg", Arrays.asList("Test"));
        return foodRepository.save(food);
    }
}
