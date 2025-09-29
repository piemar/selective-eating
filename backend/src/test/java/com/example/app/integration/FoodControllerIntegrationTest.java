package com.example.app.integration;

import com.example.app.BaseIntegrationTest;
import com.example.app.models.Food;
import com.example.app.repositories.FoodRepository;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@AutoConfigureWebMvc
class FoodControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FoodRepository foodRepository;

    @LocalServerPort
    private int port;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1/foods";
    }

    @Test
    @DisplayName("Should create a new food item and save to database")
    void shouldCreateFoodAndSaveToDatabase() {
        // Given
        Food newFood = createSampleFood("Apple", "Sweet red fruit", 
            Arrays.asList("Sweet", "Crunchy"), Arrays.asList("Fruit"));
        
        // When
        ResponseEntity<Food> response = restTemplate.postForEntity(getBaseUrl(), newFood, Food.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Apple");
        assertThat(response.getBody().getDescription()).isEqualTo("Sweet red fruit");
        assertThat(response.getBody().getId()).isNotNull();
        
        // Verify food is saved in database
        Optional<Food> savedFood = foodRepository.findByName("Apple");
        assertThat(savedFood).isPresent();
        assertThat(savedFood.get().getTags()).containsExactlyInAnyOrder("Sweet", "Crunchy");
        assertThat(savedFood.get().getCategories()).containsExactly("Fruit");
    }

    @Test
    @DisplayName("Should retrieve food by ID from database")
    void shouldRetrieveFoodByIdFromDatabase() {
        // Given
        Food food = createSampleFood("Banana", "Yellow curved fruit",
            Arrays.asList("Sweet", "Soft"), Arrays.asList("Fruit"));
        Food savedFood = foodRepository.save(food);
        
        // When
        ResponseEntity<Food> response = restTemplate.getForEntity(
            getBaseUrl() + "/" + savedFood.getId(), Food.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(savedFood.getId());
        assertThat(response.getBody().getName()).isEqualTo("Banana");
        assertThat(response.getBody().getDescription()).isEqualTo("Yellow curved fruit");
    }

    @Test
    @DisplayName("Should return 404 when food not found")
    void shouldReturn404WhenFoodNotFound() {
        // When
        ResponseEntity<Food> response = restTemplate.getForEntity(
            getBaseUrl() + "/nonexistent-id", Food.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should update existing food in database")
    void shouldUpdateExistingFoodInDatabase() {
        // Given
        Food food = createSampleFood("Orange", "Citrus fruit", 
            Arrays.asList("Citrus", "Juicy"), Arrays.asList("Fruit"));
        Food savedFood = foodRepository.save(food);
        
        Food updatedFood = createSampleFood("Orange", "Sweet citrus fruit with vitamin C",
            Arrays.asList("Citrus", "Juicy", "Sweet"), Arrays.asList("Fruit", "Healthy"));
        HttpEntity<Food> requestEntity = new HttpEntity<>(updatedFood);
        
        // When
        ResponseEntity<Food> response = restTemplate.exchange(
            getBaseUrl() + "/" + savedFood.getId(), 
            HttpMethod.PUT, 
            requestEntity, 
            Food.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(savedFood.getId());
        assertThat(response.getBody().getDescription()).isEqualTo("Sweet citrus fruit with vitamin C");
        assertThat(response.getBody().getTags()).containsExactlyInAnyOrder("Citrus", "Juicy", "Sweet");
        
        // Verify update in database
        Optional<Food> dbFood = foodRepository.findById(savedFood.getId());
        assertThat(dbFood).isPresent();
        assertThat(dbFood.get().getDescription()).isEqualTo("Sweet citrus fruit with vitamin C");
        assertThat(dbFood.get().getCategories()).containsExactlyInAnyOrder("Fruit", "Healthy");
    }

    @Test
    @DisplayName("Should delete food from database")
    void shouldDeleteFoodFromDatabase() {
        // Given
        Food food = createSampleFood("Grape", "Small round fruit", 
            Arrays.asList("Sweet", "Juicy"), Arrays.asList("Fruit"));
        Food savedFood = foodRepository.save(food);
        
        // When
        ResponseEntity<Void> response = restTemplate.exchange(
            getBaseUrl() + "/" + savedFood.getId(), 
            HttpMethod.DELETE, 
            null, 
            Void.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        
        // Verify deletion from database
        Optional<Food> deletedFood = foodRepository.findById(savedFood.getId());
        assertThat(deletedFood).isEmpty();
    }

    @Test
    @DisplayName("Should retrieve all foods with pagination from database")
    void shouldRetrieveAllFoodsWithPaginationFromDatabase() {
        // Given
        List<Food> foods = Arrays.asList(
            createSampleFood("Strawberry", "Red berry", Arrays.asList("Sweet"), Arrays.asList("Berry")),
            createSampleFood("Blueberry", "Blue berry", Arrays.asList("Sweet", "Small"), Arrays.asList("Berry")),
            createSampleFood("Raspberry", "Red berry", Arrays.asList("Sweet", "Tart"), Arrays.asList("Berry"))
        );
        foodRepository.saveAll(foods);
        
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
    @DisplayName("Should test food search by tags")
    void shouldTestFoodSearchByTags() {
        // Given
        List<Food> foods = Arrays.asList(
            createSampleFood("Sweet Apple", "Red apple", Arrays.asList("Sweet", "Crunchy"), Arrays.asList("Fruit")),
            createSampleFood("Tart Apple", "Green apple", Arrays.asList("Tart", "Crunchy"), Arrays.asList("Fruit")),
            createSampleFood("Banana", "Yellow banana", Arrays.asList("Sweet", "Soft"), Arrays.asList("Fruit"))
        );
        foodRepository.saveAll(foods);
        
        // When & Then - Test repository methods
        List<Food> sweetFoods = foodRepository.findByTagsContaining("Sweet");
        assertThat(sweetFoods).hasSize(2);
        assertThat(sweetFoods).extracting("name").containsExactlyInAnyOrder("Sweet Apple", "Banana");
        
        List<Food> crunchyFoods = foodRepository.findByTagsContaining("Crunchy");
        assertThat(crunchyFoods).hasSize(2);
        assertThat(crunchyFoods).extracting("name").containsExactlyInAnyOrder("Sweet Apple", "Tart Apple");
    }

    @Test
    @DisplayName("Should test food search by categories")
    void shouldTestFoodSearchByCategories() {
        // Given
        List<Food> foods = Arrays.asList(
            createSampleFood("Carrot", "Orange vegetable", Arrays.asList("Crunchy"), Arrays.asList("Vegetable")),
            createSampleFood("Broccoli", "Green vegetable", Arrays.asList("Healthy"), Arrays.asList("Vegetable")),
            createSampleFood("Apple", "Red fruit", Arrays.asList("Sweet"), Arrays.asList("Fruit"))
        );
        foodRepository.saveAll(foods);
        
        // When & Then - Test category search
        List<Food> vegetables = foodRepository.findByCategoriesContaining("Vegetable");
        assertThat(vegetables).hasSize(2);
        assertThat(vegetables).extracting("name").containsExactlyInAnyOrder("Carrot", "Broccoli");
        
        List<Food> fruits = foodRepository.findByCategoriesContaining("Fruit");
        assertThat(fruits).hasSize(1);
        assertThat(fruits.get(0).getName()).isEqualTo("Apple");
    }

    @Test
    @DisplayName("Should test food allergen filtering")
    void shouldTestFoodAllergenFiltering() {
        // Given
        Food nutFood = createSampleFood("Peanut", "Nut allergen food", 
            Arrays.asList("Protein"), Arrays.asList("Nuts"));
        nutFood.setIsCommonAllergen(true);
        nutFood.setAllergens(Arrays.asList("Nuts"));
        
        Food dairyFood = createSampleFood("Milk", "Dairy product", 
            Arrays.asList("Creamy"), Arrays.asList("Dairy"));
        dairyFood.setIsCommonAllergen(true);
        dairyFood.setAllergens(Arrays.asList("Dairy"));
        
        Food safeFood = createSampleFood("Apple", "Safe fruit", 
            Arrays.asList("Sweet"), Arrays.asList("Fruit"));
        safeFood.setIsCommonAllergen(false);
        safeFood.setAllergens(Arrays.asList());
        
        foodRepository.saveAll(Arrays.asList(nutFood, dairyFood, safeFood));
        
        // When & Then - Test allergen filtering
        List<Food> nonAllergenFoods = foodRepository.findFoodsWithoutAllergens(Arrays.asList("Nuts", "Dairy"));
        assertThat(nonAllergenFoods).hasSize(1);
        assertThat(nonAllergenFoods.get(0).getName()).isEqualTo("Apple");
    }

    @Test
    @DisplayName("Should test food texture and flavor properties")
    void shouldTestFoodTextureAndFlavorProperties() {
        // Given
        Food crunchyFood = createSampleFood("Carrot", "Crunchy vegetable", 
            Arrays.asList("Healthy"), Arrays.asList("Vegetable"));
        crunchyFood.setTextureProperties(Arrays.asList("Crunchy", "Hard"));
        crunchyFood.setFlavorProperties(Arrays.asList("Mild", "Sweet"));
        
        Food softFood = createSampleFood("Banana", "Soft fruit", 
            Arrays.asList("Sweet"), Arrays.asList("Fruit"));
        softFood.setTextureProperties(Arrays.asList("Soft", "Smooth"));
        softFood.setFlavorProperties(Arrays.asList("Sweet", "Mild"));
        
        foodRepository.saveAll(Arrays.asList(crunchyFood, softFood));
        
        // When & Then - Test texture search
        List<Food> crunchyFoods = foodRepository.findByTexturePropertiesContaining("Crunchy");
        assertThat(crunchyFoods).hasSize(1);
        assertThat(crunchyFoods.get(0).getName()).isEqualTo("Carrot");
        
        List<Food> softFoods = foodRepository.findByTexturePropertiesContaining("Soft");
        assertThat(softFoods).hasSize(1);
        assertThat(softFoods.get(0).getName()).isEqualTo("Banana");
        
        // Test flavor search
        List<Food> sweetFoods = foodRepository.findByFlavorPropertiesContaining("Sweet");
        assertThat(sweetFoods).hasSize(2);
        assertThat(sweetFoods).extracting("name").containsExactlyInAnyOrder("Carrot", "Banana");
    }

    @Test
    @DisplayName("Should test similar foods recommendation query")
    void shouldTestSimilarFoodsRecommendationQuery() {
        // Given
        Food apple = createSampleFood("Apple", "Crunchy fruit", 
            Arrays.asList("Sweet"), Arrays.asList("Fruit"));
        apple.setTextureProperties(Arrays.asList("Crunchy", "Firm"));
        apple.setFlavorProperties(Arrays.asList("Sweet", "Fresh"));
        apple.setVisualProperties(Arrays.asList("Round", "Colorful"));
        
        Food pear = createSampleFood("Pear", "Soft fruit", 
            Arrays.asList("Sweet"), Arrays.asList("Fruit"));
        pear.setTextureProperties(Arrays.asList("Soft", "Juicy"));
        pear.setFlavorProperties(Arrays.asList("Sweet", "Mild"));
        pear.setVisualProperties(Arrays.asList("Round", "Green"));
        
        Food carrot = createSampleFood("Carrot", "Crunchy vegetable", 
            Arrays.asList("Healthy"), Arrays.asList("Vegetable"));
        carrot.setTextureProperties(Arrays.asList("Crunchy", "Hard"));
        carrot.setFlavorProperties(Arrays.asList("Mild", "Earthy"));
        carrot.setVisualProperties(Arrays.asList("Long", "Orange"));
        
        foodRepository.saveAll(Arrays.asList(apple, pear, carrot));
        
        // When & Then - Test similar foods query
        List<Food> similarToApple = foodRepository.findSimilarFoods(
            Arrays.asList("Crunchy", "Firm"),
            Arrays.asList("Sweet", "Fresh"),
            Arrays.asList("Round", "Colorful")
        );
        
        // Should find foods with similar properties
        assertThat(similarToApple).hasSizeGreaterThan(0);
        // Apple should match itself and potentially others with similar properties
    }

    @Test
    @DisplayName("Should handle name-based search with case insensitivity")
    void shouldHandleNameBasedSearchWithCaseInsensitivity() {
        // Given
        List<Food> foods = Arrays.asList(
            createSampleFood("Apple Juice", "Fruit drink", Arrays.asList("Sweet"), Arrays.asList("Beverage")),
            createSampleFood("Green Apple", "Tart apple", Arrays.asList("Tart"), Arrays.asList("Fruit")),
            createSampleFood("Pineapple", "Tropical fruit", Arrays.asList("Sweet"), Arrays.asList("Fruit"))
        );
        foodRepository.saveAll(foods);
        
        // When & Then - Test case insensitive search
        List<Food> appleResults = foodRepository.findByNameContainingIgnoreCase("apple");
        assertThat(appleResults).hasSize(3);
        assertThat(appleResults).extracting("name")
            .containsExactlyInAnyOrder("Apple Juice", "Green Apple", "Pineapple");
        
        List<Food> upperCaseResults = foodRepository.findByNameContainingIgnoreCase("APPLE");
        assertThat(upperCaseResults).hasSize(3);
    }

    private Food createSampleFood(String name, String description, List<String> tags, List<String> categories) {
        Food food = new Food(name, description, "https://example.com/image.jpg", tags);
        food.setCategories(categories);
        return food;
    }
}
