package com.example.app.services;

import com.example.app.models.FoodEmbeddedCodes;
import com.example.app.repositories.FoodEmbeddedCodesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for food operations using the optimized embedded codes structure.
 * 
 * This service provides high-level operations on the foods_embedded_codes collection
 * with efficient queries and easy access to classification data.
 */
@Service
public class FoodService {
    
    @Autowired
    private FoodEmbeddedCodesRepository foodRepository;
    
    // ===== BASIC FOOD OPERATIONS =====
    
    /**
     * Get food by foodNumber (unique ID) in specific language.
     */
    public Optional<FoodEmbeddedCodes> getFoodByNumber(Integer foodNumber, String language) {
        List<FoodEmbeddedCodes> foods = foodRepository.findByFoodNumber(foodNumber)
            .map(List::of)
            .orElse(List.of())
            .stream()
            .filter(food -> language.equals(food.getLanguage()))
            .collect(Collectors.toList());
        
        return foods.isEmpty() ? Optional.empty() : Optional.of(foods.get(0));
    }
    
    /**
     * Get food by foodNumber.
     */
    public Optional<FoodEmbeddedCodes> getFoodByNumber(Integer foodNumber) {
        return foodRepository.findByFoodNumber(foodNumber);
    }
    
    /**
     * Get all foods.
     */
    public List<FoodEmbeddedCodes> getAllFoods() {
        return foodRepository.findAll();
    }
    
    /**
     * Get foods with pagination.
     */
    public Page<FoodEmbeddedCodes> getAllFoods(Pageable pageable) {
        return foodRepository.findAll(pageable);
    }
    
    /**
     * Get foods by language.
     */
    public List<FoodEmbeddedCodes> getFoodsByLanguage(String language) {
        return foodRepository.findByLanguage(language);
    }
    
    /**
     * Get foods by language with pagination.
     */
    public Page<FoodEmbeddedCodes> getFoodsByLanguage(String language, Pageable pageable) {
        List<FoodEmbeddedCodes> foods = foodRepository.findByLanguage(language);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), foods.size());
        
        List<FoodEmbeddedCodes> pagedFoods = foods.subList(start, end);
        return new PageImpl<>(pagedFoods, pageable, foods.size());
    }
    
    /**
     * Get foods by category.
     */
    public List<FoodEmbeddedCodes> getFoodsByCategory(String category) {
        return foodRepository.findByFoodCategory(category);
    }
    
    /**
     * Get foods by category with pagination.
     */
    public Page<FoodEmbeddedCodes> getFoodsByCategory(String category, Pageable pageable) {
        List<FoodEmbeddedCodes> foods = foodRepository.findByFoodCategory(category);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), foods.size());
        
        List<FoodEmbeddedCodes> pagedFoods = foods.subList(start, end);
        return new PageImpl<>(pagedFoods, pageable, foods.size());
    }
    
    /**
     * Search foods by name (case insensitive).
     * Uses proper @JsonProperty field mapping from Swedish MongoDB data.
     */
    public List<FoodEmbeddedCodes> searchFoodsByName(String name) {
        // Get all foods and filter by name - @JsonProperty handles Swedish->English mapping
        return foodRepository.findAll().stream()
            .filter(food -> food.getName() != null && 
                           food.getName().toLowerCase().contains(name.toLowerCase()))
            .collect(Collectors.toList());
    }
    
    /**
     * Get animal-based foods.
     * Safe fallback for Swedish data without classifications.
     */
    public List<FoodEmbeddedCodes> getAnimalFoods() {
        try {
            return foodRepository.findAnimalProducts();
        } catch (Exception e) {
            // Swedish data fallback - search by food name
            return foodRepository.findAll().stream()
                    .filter(food -> food.getName() != null && 
                           (food.getName().toLowerCase().contains("kött") ||
                            food.getName().toLowerCase().contains("fisk") ||
                            food.getName().toLowerCase().contains("mjölk") ||
                            food.getName().toLowerCase().contains("meat") ||
                            food.getName().toLowerCase().contains("fish") ||
                            food.getName().toLowerCase().contains("milk")))
                    .collect(java.util.stream.Collectors.toList());
        }
    }
    
    /**
     * Get plant-based foods.
     * Safe fallback for Swedish data without classifications.
     */
    public List<FoodEmbeddedCodes> getPlantFoods() {
        try {
            return foodRepository.findPlantBasedProducts();
        } catch (Exception e) {
            // Swedish data fallback - search by food name
            return foodRepository.findAll().stream()
                    .filter(food -> food.getName() != null && 
                           (food.getName().toLowerCase().contains("frukt") ||
                            food.getName().toLowerCase().contains("grönsak") ||
                            food.getName().toLowerCase().contains("bröd") ||
                            food.getName().toLowerCase().contains("fruit") ||
                            food.getName().toLowerCase().contains("vegetable") ||
                            food.getName().toLowerCase().contains("bread")))
                    .collect(java.util.stream.Collectors.toList());
        }
    }
    
    /**
     * Get total count of foods.
     */
    public long getTotalFoodCount() {
        return foodRepository.count();
    }
    
    /**
     * Get count by language.
     */
    public long getFoodCountByLanguage(String language) {
        return foodRepository.findByLanguage(language).size();
    }
    
    /**
     * Get unique food categories.
     */
    public List<String> getAllCategories() {
        return foodRepository.findAll().stream()
            .map(FoodEmbeddedCodes::getFoodCategory)
            .filter(category -> category != null)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }
    
    /**
     * Get total food count.
     */
    public long getTotalFoodCount() {
        return foodRepository.count();
    }
    
    /**
     * Get food count by language.
     */
    public long getFoodCountByLanguage(String language) {
        return foodRepository.findByLanguage(language).size();
    }
}