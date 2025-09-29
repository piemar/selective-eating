package com.example.app.repositories;

import com.example.app.models.FoodExperience;
import com.example.app.models.Child;
import com.example.app.models.Food;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing FoodExperience data.
 * Updated to match integration test expectations.
 */
@Repository
public interface FoodExperienceRepository extends MongoRepository<FoodExperience, String> {
    
    // Child-based queries
    List<FoodExperience> findByChild(Child child);
    List<FoodExperience> findByChildOrderByCreatedAtDesc(Child child);
    List<FoodExperience> findByChildId(String childId);
    
    // Food-based queries
    List<FoodExperience> findByFood(Food food);
    List<FoodExperience> findByFoodId(String foodId);
    
    // Combined child and food queries
    Optional<FoodExperience> findByChildAndFood(Child child, Food food);
    List<FoodExperience> findByChildAndFoodId(Child child, String foodId);
    
    // Reaction-based queries
    List<FoodExperience> findByReaction(String reaction);
    List<FoodExperience> findByChildAndReaction(Child child, String reaction);
    
    // Rating-based queries
    List<FoodExperience> findByRating(Integer rating);
    List<FoodExperience> findByChildAndRating(Child child, Integer rating);
    List<FoodExperience> findByRatingGreaterThanEqual(Integer rating);
    List<FoodExperience> findByChildAndRatingGreaterThanEqual(Child child, Integer rating);
    
    // Time-based queries
    List<FoodExperience> findByCreatedAtAfter(Instant after);
    List<FoodExperience> findByChildAndCreatedAtAfter(Child child, Instant after);
    List<FoodExperience> findByCreatedAtBetween(Instant start, Instant end);
    List<FoodExperience> findByChildAndCreatedAtBetween(Child child, Instant start, Instant end);
    
    // Experience context queries
    List<FoodExperience> findByWasFirstTime(Boolean wasFirstTime);
    List<FoodExperience> findByChildAndWasFirstTime(Child child, Boolean wasFirstTime);
    List<FoodExperience> findByMealType(String mealType);
    List<FoodExperience> findByChildAndMealType(Child child, String mealType);
    List<FoodExperience> findByEnvironment(String environment);
    List<FoodExperience> findByChildAndEnvironment(Child child, String environment);
    List<FoodExperience> findByMood(String mood);
    List<FoodExperience> findByChildAndMood(Child child, String mood);
    
    // Legacy methods for backward compatibility (using userId)
    List<FoodExperience> findByUserIdOrderByCreatedAtDesc(String userId);
    List<FoodExperience> findByUserIdAndFoodNumber(String userId, Integer foodNumber);
    List<FoodExperience> findByFoodNumber(Integer foodNumber);
    List<FoodExperience> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(String userId, Instant startDate, Instant endDate);
    long countByUserId(String userId);
    
    /**
     * Find positive experiences (rating 4 or 5) for a child.
     */
    @Query("{ 'child': ?0, 'rating': { $gte: 4 } }")
    List<FoodExperience> findPositiveExperiencesByChild(Child child);
    
    /**
     * Find positive experiences (rating 4 or 5) by userId (legacy).
     */
    @Query("{ 'userId': ?0, 'rating': { $gte: 4 } }")
    List<FoodExperience> findPositiveExperiencesByUserId(String userId);
    
    /**
     * Count positive experiences for a child.
     */
    @Query(value = "{ 'child': ?0, 'rating': { $gte: 4 } }", count = true)
    long countPositiveExperiencesByChild(Child child);
    
    /**
     * Count positive experiences by userId (legacy).
     */
    @Query(value = "{ 'userId': ?0, 'rating': { $gte: 4 } }", count = true)
    long countPositiveExperiencesByUserId(String userId);
    
    /**
     * Get the child's food IDs with positive experiences.
     */
    @Query(value = "{ 'child': ?0, 'rating': { $gte: 4 } }", fields = "{ 'food': 1 }")
    List<FoodExperience> findLikedFoodsByChild(Child child);
    
    /**
     * Get liked food numbers by userId (legacy).
     */
    @Query(value = "{ 'userId': ?0, 'rating': { $gte: 4 } }", fields = "{ 'foodNumber': 1 }")
    List<FoodExperience> findLikedFoodNumbersByUserId(String userId);
    
    /**
     * Find recent experiences (last 7 days) for a child.
     */
    @Query("{ 'child': ?0, 'createdAt': { $gte: ?1 } }")
    List<FoodExperience> findRecentExperiencesByChild(Child child, Instant sevenDaysAgo);
    
    /**
     * Find recent experiences by userId (legacy).
     */
    @Query("{ 'userId': ?0, 'createdAt': { $gte: ?1 } }")
    List<FoodExperience> findRecentExperiencesByUserId(String userId, Instant sevenDaysAgo);
    
    /**
     * Get ratings for a specific food across all children.
     */
    @Query(value = "{ 'food': ?0 }", fields = "{ 'rating': 1 }")
    List<FoodExperience> findRatingsByFood(Food food);
    
    /**
     * Get ratings by food number (legacy).
     */
    @Query(value = "{ 'foodNumber': ?0 }", fields = "{ 'rating': 1 }")
    List<FoodExperience> findRatingsByFoodNumber(Integer foodNumber);
}