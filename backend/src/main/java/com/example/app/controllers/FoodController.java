package com.example.app.controllers;

import com.example.app.models.FoodEmbeddedCodes;
import com.example.app.services.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for food operations.
 * Provides basic CRUD operations for the optimized food data.
 */
@RestController
@RequestMapping("/api/v1/foods")
@CrossOrigin(origins = "*")
public class FoodController {
    
    @Autowired
    private FoodService foodService;
    
    /**
     * Get all foods with pagination.
     */
    @GetMapping
    public Page<FoodEmbeddedCodes> getAllFoods(Pageable pageable) {
        return foodService.getAllFoods(pageable);
    }
    
    /**
     * Get food by food number.
     */
    @GetMapping("/{foodNumber}")
    public ResponseEntity<FoodEmbeddedCodes> getFoodByNumber(@PathVariable Integer foodNumber) {
        Optional<FoodEmbeddedCodes> food = foodService.getFoodByNumber(foodNumber);
        return food.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get food by food number and language.
     */
    @GetMapping("/{foodNumber}/{language}")
    public ResponseEntity<FoodEmbeddedCodes> getFoodByNumberAndLanguage(
            @PathVariable Integer foodNumber, 
            @PathVariable String language) {
        Optional<FoodEmbeddedCodes> food = foodService.getFoodByNumber(foodNumber, language);
        return food.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get foods by language.
     */
    @GetMapping("/language/{language}")
    public List<FoodEmbeddedCodes> getFoodsByLanguage(@PathVariable String language) {
        return foodService.getFoodsByLanguage(language);
    }
    
    /**
     * Get foods by language with pagination.
     */
    @GetMapping("/language/{language}/paged")
    public Page<FoodEmbeddedCodes> getFoodsByLanguageWithPaging(
            @PathVariable String language, 
            Pageable pageable) {
        return foodService.getFoodsByLanguage(language, pageable);
    }
    
    /**
     * Search foods by name.
     */
    @GetMapping("/search")
    public List<FoodEmbeddedCodes> searchFoods(@RequestParam String name) {
        return foodService.searchFoodsByName(name);
    }
    
    /**
     * Get foods by category.
     */
    @GetMapping("/category/{category}")
    public List<FoodEmbeddedCodes> getFoodsByCategory(@PathVariable String category) {
        return foodService.getFoodsByCategory(category);
    }
    
    /**
     * Get foods by category with pagination.
     */
    @GetMapping("/category/{category}/paged")
    public Page<FoodEmbeddedCodes> getFoodsByCategoryWithPaging(
            @PathVariable String category, 
            Pageable pageable) {
        return foodService.getFoodsByCategory(category, pageable);
    }
    
    /**
     * Get all unique food categories.
     */
    @GetMapping("/categories")
    public List<String> getAllCategories() {
        return foodService.getAllCategories();
    }
    
    /**
     * Get animal-based foods.
     */
    @GetMapping("/animal")
    public List<FoodEmbeddedCodes> getAnimalFoods() {
        return foodService.getAnimalFoods();
    }
    
    /**
     * Get plant-based foods.
     */
    @GetMapping("/plant")
    public List<FoodEmbeddedCodes> getPlantFoods() {
        return foodService.getPlantFoods();
    }
    
    /**
     * Get total food count.
     */
    @GetMapping("/count")
    public long getTotalFoodCount() {
        return foodService.getTotalFoodCount();
    }
    
    /**
     * Get food count by language.
     */
    @GetMapping("/count/{language}")
    public long getFoodCountByLanguage(@PathVariable String language) {
        return foodService.getFoodCountByLanguage(language);
    }
}