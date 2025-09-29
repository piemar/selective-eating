package com.example.app.controllers;

import com.example.app.models.FoodExperience;
import com.example.app.services.FoodExperienceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing food experiences and logging.
 */
@RestController
@RequestMapping("/api/v1/experiences")
public class FoodExperienceController {

    @Autowired
    private FoodExperienceService experienceService;

    /**
     * Log a new food experience.
     */
    @PostMapping
    public ResponseEntity<FoodExperience> logExperience(@Valid @RequestBody LogExperienceRequest request) {
        FoodExperience experience = experienceService.logExperience(
            request.getUserId(),
            request.getFoodNumber(),
            request.getFoodName(),
            request.getRating(),
            request.getNotes(),
            request.getContext()
        );
        return ResponseEntity.ok(experience);
    }

    /**
     * Get all experiences for a user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FoodExperience>> getUserExperiences(@PathVariable String userId) {
        List<FoodExperience> experiences = experienceService.getUserExperiences(userId);
        return ResponseEntity.ok(experiences);
    }

    /**
     * Get user statistics and progress.
     */
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<UserStats> getUserStats(@PathVariable String userId) {
        UserStats stats = experienceService.getUserStats(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get foods that the user has liked (rating 4+).
     */
    @GetMapping("/user/{userId}/liked-foods")
    public ResponseEntity<List<Integer>> getUserLikedFoods(@PathVariable String userId) {
        List<Integer> likedFoods = experienceService.getLikedFoodNumbers(userId);
        return ResponseEntity.ok(likedFoods);
    }

    /**
     * Update an existing food experience.
     */
    @PutMapping("/{experienceId}")
    public ResponseEntity<FoodExperience> updateExperience(
            @PathVariable String experienceId,
            @Valid @RequestBody UpdateExperienceRequest request) {
        FoodExperience experience = experienceService.updateExperience(
            experienceId,
            request.getRating(),
            request.getNotes()
        );
        return ResponseEntity.ok(experience);
    }

    /**
     * Delete a food experience.
     */
    @DeleteMapping("/{experienceId}")
    public ResponseEntity<Void> deleteExperience(@PathVariable String experienceId) {
        experienceService.deleteExperience(experienceId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Request DTO for logging experiences.
     */
    public static class LogExperienceRequest {
        private String userId;
        private Integer foodNumber;
        private String foodName;
        private Integer rating;
        private String notes;
        private String context;

        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public Integer getFoodNumber() { return foodNumber; }
        public void setFoodNumber(Integer foodNumber) { this.foodNumber = foodNumber; }

        public String getFoodName() { return foodName; }
        public void setFoodName(String foodName) { this.foodName = foodName; }

        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public String getContext() { return context; }
        public void setContext(String context) { this.context = context; }
    }

    /**
     * Request DTO for updating experiences.
     */
    public static class UpdateExperienceRequest {
        private Integer rating;
        private String notes;

        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    /**
     * Response DTO for user statistics.
     */
    public static class UserStats {
        private long totalFoodsTried;
        private long positiveFoods;
        private double positivePercentage;
        private long streak;
        private List<String> recentAchievements;

        public UserStats() {}

        public UserStats(long totalFoodsTried, long positiveFoods, double positivePercentage, 
                        long streak, List<String> recentAchievements) {
            this.totalFoodsTried = totalFoodsTried;
            this.positiveFoods = positiveFoods;
            this.positivePercentage = positivePercentage;
            this.streak = streak;
            this.recentAchievements = recentAchievements;
        }

        // Getters and setters
        public long getTotalFoodsTried() { return totalFoodsTried; }
        public void setTotalFoodsTried(long totalFoodsTried) { this.totalFoodsTried = totalFoodsTried; }

        public long getPositiveFoods() { return positiveFoods; }
        public void setPositiveFoods(long positiveFoods) { this.positiveFoods = positiveFoods; }

        public double getPositivePercentage() { return positivePercentage; }
        public void setPositivePercentage(double positivePercentage) { this.positivePercentage = positivePercentage; }

        public long getStreak() { return streak; }
        public void setStreak(long streak) { this.streak = streak; }

        public List<String> getRecentAchievements() { return recentAchievements; }
        public void setRecentAchievements(List<String> recentAchievements) { this.recentAchievements = recentAchievements; }
    }
}
