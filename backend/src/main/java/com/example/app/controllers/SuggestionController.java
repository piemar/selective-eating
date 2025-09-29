package com.example.app.controllers;

import com.example.app.models.FoodSuggestion;
import com.example.app.services.FoodSuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for AI food suggestions.
 */
@RestController
@RequestMapping("/api/v1/suggestions")
public class SuggestionController {

    @Autowired
    private FoodSuggestionService suggestionService;

    /**
     * Get personalized food suggestions based on liked foods.
     */
    @PostMapping
    public ResponseEntity<List<FoodSuggestion>> getSuggestions(@RequestBody SuggestionRequest request) {
        List<FoodSuggestion> suggestions = suggestionService.generateSuggestions(
            request.getLikedFoodNumbers(), 
            request.getMaxSuggestions() != null ? request.getMaxSuggestions() : 5
        );
        return ResponseEntity.ok(suggestions);
    }

    /**
     * Get popular food suggestions for new users.
     */
    @GetMapping("/popular")
    public ResponseEntity<List<FoodSuggestion>> getPopularSuggestions(
            @RequestParam(defaultValue = "6") int maxSuggestions) {
        List<FoodSuggestion> suggestions = suggestionService.getPopularFoodSuggestions(maxSuggestions);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * Request DTO for getting suggestions.
     */
    public static class SuggestionRequest {
        private List<Integer> likedFoodNumbers;
        private Integer maxSuggestions;

        // Getters and setters
        public List<Integer> getLikedFoodNumbers() { return likedFoodNumbers; }
        public void setLikedFoodNumbers(List<Integer> likedFoodNumbers) { this.likedFoodNumbers = likedFoodNumbers; }

        public Integer getMaxSuggestions() { return maxSuggestions; }
        public void setMaxSuggestions(Integer maxSuggestions) { this.maxSuggestions = maxSuggestions; }
    }
}
