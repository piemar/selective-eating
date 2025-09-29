package com.example.app.services;

import com.example.app.models.FoodEmbeddedCodes;
import com.example.app.models.FoodSuggestion;
import com.example.app.models.FoodExperience;
import com.example.app.repositories.FoodEmbeddedCodesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating intelligent food suggestions based on user preferences.
 * Uses food classification data and user history to suggest similar foods.
 */
@Service
public class FoodSuggestionService {

    @Autowired
    private FoodEmbeddedCodesRepository foodRepository;
    
    /**
     * Generate food suggestions based on foods the user/child likes.
     */
    public List<FoodSuggestion> generateSuggestions(List<Integer> likedFoodNumbers, int maxSuggestions) {
        if (likedFoodNumbers == null || likedFoodNumbers.isEmpty()) {
            return getPopularFoodSuggestions(maxSuggestions);
        }
        
        // Get the foods the user likes
        List<FoodEmbeddedCodes> likedFoods = foodRepository.findAllByFoodNumberIn(likedFoodNumbers);
        if (likedFoods.isEmpty()) {
            return getPopularFoodSuggestions(maxSuggestions);
        }
        
        // Analyze preferences
        FoodPreferenceProfile profile = analyzePreferences(likedFoods);
        
        // Find similar foods
        List<FoodEmbeddedCodes> allFoods = foodRepository.findByLanguage("en");
        
        List<FoodSuggestion> suggestions = allFoods.stream()
                .filter(food -> !likedFoodNumbers.contains(food.getFoodNumber())) // Exclude already liked foods
                .map(food -> scoreFoodSimilarity(food, profile, likedFoods))
                .filter(suggestion -> suggestion.getConfidenceScore() > 0.3) // Only confident suggestions
                .sorted((s1, s2) -> Double.compare(s2.getConfidenceScore(), s1.getConfidenceScore()))
                .limit(maxSuggestions)
                .collect(Collectors.toList());
                
        return suggestions;
    }
    
    /**
     * Get popular food suggestions for new users.
     */
    public List<FoodSuggestion> getPopularFoodSuggestions(int maxSuggestions) {
        // Get child-friendly foods from different categories
        List<FoodEmbeddedCodes> popularFoods = foodRepository.findByLanguage("en");
        
        return popularFoods.stream()
                .filter(this::isChildFriendly)
                .limit(maxSuggestions)
                .map(food -> new FoodSuggestion(
                    food.getFoodNumber(),
                    food.getName(),
                    food.getImageUrl(),
                    extractTags(food),
                    "Popular choice for children - mild flavor and familiar texture",
                    0.8,
                    new ArrayList<>()
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * Analyze food preferences from liked foods.
     */
    private FoodPreferenceProfile analyzePreferences(List<FoodEmbeddedCodes> likedFoods) {
        Map<String, Integer> categoryCount = new HashMap<>();
        Map<String, Integer> sourceCount = new HashMap<>();
        Set<String> preferredTags = new HashSet<>();
        
        for (FoodEmbeddedCodes food : likedFoods) {
            // Count food categories
            if (food.getFoodCategory() != null) {
                categoryCount.merge(food.getFoodCategory(), 1, Integer::sum);
            }
            
            // Analyze food source (plant vs animal)
            if (food.isPlantBased()) {
                sourceCount.merge("plant", 1, Integer::sum);
            } else if (food.isAnimalBased()) {
                sourceCount.merge("animal", 1, Integer::sum);
            }
            
            // Extract preferred characteristics
            preferredTags.addAll(extractTags(food));
        }
        
        return new FoodPreferenceProfile(categoryCount, sourceCount, preferredTags);
    }
    
    /**
     * Score how similar a food is to the user's preferences.
     */
    private FoodSuggestion scoreFoodSimilarity(FoodEmbeddedCodes candidate, FoodPreferenceProfile profile, List<FoodEmbeddedCodes> likedFoods) {
        double score = 0.0;
        List<String> reasons = new ArrayList<>();
        
        // Category similarity (40% weight)
        if (candidate.getFoodCategory() != null && profile.preferredCategories.containsKey(candidate.getFoodCategory())) {
            score += 0.4 * (profile.preferredCategories.get(candidate.getFoodCategory()) / (double) likedFoods.size());
            reasons.add("same food category");
        }
        
        // Source similarity (20% weight) 
        if (candidate.isPlantBased() && profile.preferredSources.getOrDefault("plant", 0) > 0) {
            score += 0.2;
            reasons.add("plant-based like your other favorites");
        } else if (candidate.isAnimalBased() && profile.preferredSources.getOrDefault("animal", 0) > 0) {
            score += 0.2;
            reasons.add("similar protein source");
        }
        
        // Tag similarity (30% weight)
        List<String> candidateTags = extractTags(candidate);
        long matchingTags = candidateTags.stream()
                .mapToLong(tag -> profile.preferredTags.contains(tag) ? 1 : 0)
                .sum();
        if (!candidateTags.isEmpty()) {
            score += 0.3 * (matchingTags / (double) candidateTags.size());
            if (matchingTags > 0) {
                reasons.add("similar texture and taste");
            }
        }
        
        // Novelty bonus (10% weight) - slightly favor foods from different categories for variety
        if (candidate.getFoodCategory() != null && !profile.preferredCategories.containsKey(candidate.getFoodCategory())) {
            score += 0.1;
            reasons.add("introduces variety to your child's diet");
        }
        
        String reason = reasons.isEmpty() ? "Recommended for expanding food preferences" : 
                       "Great choice because it has " + String.join(", ", reasons);
        
        return new FoodSuggestion(
            candidate.getFoodNumber(),
            candidate.getName(),
            candidate.getImageUrl(),
            candidateTags,
            reason,
            Math.min(score, 1.0), // Cap at 1.0
            likedFoods.stream().map(FoodEmbeddedCodes::getFoodNumber).collect(Collectors.toList())
        );
    }
    
    /**
     * Extract tags/characteristics from a food item.
     */
    private List<String> extractTags(FoodEmbeddedCodes food) {
        List<String> tags = new ArrayList<>();
        
        // Add tags based on food category
        String category = food.getFoodCategory();
        if (category != null) {
            if (category.toLowerCase().contains("fruit")) tags.add("Sweet");
            if (category.toLowerCase().contains("vegetable")) tags.add("Healthy");
            if (category.toLowerCase().contains("dairy")) tags.add("Creamy");
            if (category.toLowerCase().contains("grain")) tags.add("Mild");
            if (category.toLowerCase().contains("meat")) tags.add("Protein");
        }
        
        // Add tags based on food source
        if (food.isPlantBased()) {
            tags.add("Plant-based");
        }
        if (food.isAnimalBased()) {
            tags.add("Protein-rich");
        }
        
        // Add general child-friendly tags
        if (isChildFriendly(food)) {
            tags.add("Kid-friendly");
        }
        
        return tags.stream().distinct().collect(Collectors.toList());
    }
    
    /**
     * Check if a food is typically child-friendly.
     */
    private boolean isChildFriendly(FoodEmbeddedCodes food) {
        String name = food.getName().toLowerCase();
        String category = food.getFoodCategory() != null ? food.getFoodCategory().toLowerCase() : "";
        
        // Positive indicators for child-friendly foods
        return name.contains("apple") || name.contains("banana") || name.contains("pasta") ||
               name.contains("rice") || name.contains("chicken") || name.contains("cheese") ||
               name.contains("bread") || name.contains("yogurt") || name.contains("milk") ||
               category.contains("fruit") || category.contains("dairy") ||
               (category.contains("cereal") && !name.contains("spice"));
    }
    
    /**
     * Inner class to represent user food preferences.
     */
    private static class FoodPreferenceProfile {
        final Map<String, Integer> preferredCategories;
        final Map<String, Integer> preferredSources;
        final Set<String> preferredTags;
        
        FoodPreferenceProfile(Map<String, Integer> categories, Map<String, Integer> sources, Set<String> tags) {
            this.preferredCategories = categories;
            this.preferredSources = sources;
            this.preferredTags = tags;
        }
    }
}
