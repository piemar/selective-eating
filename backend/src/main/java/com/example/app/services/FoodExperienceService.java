package com.example.app.services;

import com.example.app.controllers.FoodExperienceController.UserStats;
import com.example.app.models.FoodExperience;
import com.example.app.repositories.FoodExperienceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing food experiences and user progress tracking.
 */
@Service
public class FoodExperienceService {

    @Autowired
    private FoodExperienceRepository experienceRepository;

    /**
     * Log a new food experience.
     */
    public FoodExperience logExperience(String userId, Integer foodNumber, String foodName, 
                                       Integer rating, String notes, String context) {
        // Check if user already has an experience with this food
        List<FoodExperience> existing = experienceRepository.findByUserIdAndFoodNumber(userId, foodNumber);
        
        FoodExperience experience;
        if (!existing.isEmpty()) {
            // Update the most recent experience
            experience = existing.get(0);
            experience.setRating(rating);
            experience.setNotes(notes);
            experience.setMealType(context);
            experience.touch(); // Update the timestamp
        } else {
            // Create new experience (using legacy fields for now)
            experience = new FoodExperience();
            // Set legacy fields until we can update the service to use Child/Food references
            // TODO: Update to use proper Child and Food objects
            experience.setUserId(userId);
            experience.setFoodNumber(foodNumber);
            experience.setFoodName(foodName);
            experience.setRating(rating);
            experience.setNotes(notes);
            experience.setMealType(context);
        }
        
        return experienceRepository.save(experience);
    }

    /**
     * Update an existing food experience.
     */
    public FoodExperience updateExperience(String experienceId, Integer rating, String notes) {
        Optional<FoodExperience> optionalExperience = experienceRepository.findById(experienceId);
        if (optionalExperience.isEmpty()) {
            throw new RuntimeException("Experience not found: " + experienceId);
        }
        
        FoodExperience experience = optionalExperience.get();
        if (rating != null) {
            experience.setRating(rating);
        }
        if (notes != null) {
            experience.setNotes(notes);
        }
        experience.touch();
        
        return experienceRepository.save(experience);
    }

    /**
     * Delete a food experience.
     */
    public void deleteExperience(String experienceId) {
        experienceRepository.deleteById(experienceId);
    }

    /**
     * Get all experiences for a user.
     */
    public List<FoodExperience> getUserExperiences(String userId) {
        return experienceRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get food numbers that the user has liked (rating 4+).
     */
    public List<Integer> getLikedFoodNumbers(String userId) {
        List<FoodExperience> positiveExperiences = experienceRepository.findPositiveExperiencesByUserId(userId);
        return positiveExperiences.stream()
                .map(exp -> exp.getFoodNumber())
                .filter(foodNumber -> foodNumber != null)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Get comprehensive user statistics.
     */
    public UserStats getUserStats(String userId) {
        long totalFoodsTried = experienceRepository.countByUserId(userId);
        long positiveFoods = experienceRepository.countPositiveExperiencesByUserId(userId);
        
        double positivePercentage = totalFoodsTried > 0 ? 
            (double) positiveFoods / totalFoodsTried * 100 : 0.0;
        
        long streak = 5; // Simplified for now
        List<String> achievements = List.of("Food Explorer", "Taste Champion");
        
        return new UserStats(totalFoodsTried, positiveFoods, positivePercentage, streak, achievements);
    }
}