package com.example.app.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.index.Indexed;
import java.time.Instant;

/**
 * Represents a comprehensive food experience/rating from a child.
 * Updated to match integration test expectations.
 */
@Document("food_experiences")
public class FoodExperience {
    
    @Id
    private String id;
    
    @Indexed
    @DBRef
    private Child child;                 // Reference to the child who had this experience
    
    @Indexed
    @DBRef
    private Food food;                   // Reference to the food that was tried
    
    // Legacy fields for backwards compatibility
    @Indexed
    private String userId;               // Legacy: User who logged this experience (session-based)
    
    @Indexed 
    private Integer foodNumber;          // Legacy: Reference to the food that was tried
    private String foodName;             // Legacy: Name of the food for easy reference
    
    private Integer rating;              // 1-5 star rating
    private String notes;                // Optional notes about the experience
    private String reaction;             // "liked", "disliked", "neutral", etc.
    
    // Experience context
    private Boolean wasFirstTime;        // Whether this was the first time trying this food
    private String mealType;             // "breakfast", "lunch", "dinner", "snack"
    private String environment;          // "home", "school", "restaurant", etc.
    private String mood;                 // Child's mood during the experience
    
    // Food preparation and serving details
    private String portion;              // "small", "medium", "large"
    private String preparationMethod;    // "raw", "cooked", "grilled", etc.
    private String accompaniedBy;        // What the food was served with
    
    // Future intentions
    private Boolean willTryAgain;        // Whether the child would try this food again
    
    @Indexed
    private Instant createdAt;
    private Instant updatedAt;
    
    // Constructors
    public FoodExperience() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    public FoodExperience(Child child, Food food, Integer rating, String notes) {
        this();
        this.child = child;
        this.food = food;
        this.rating = rating;
        this.notes = notes;
        this.reaction = deriveReactionFromRating(rating);
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Child getChild() { return child; }
    public void setChild(Child child) { this.child = child; }
    
    public Food getFood() { return food; }
    public void setFood(Food food) { this.food = food; }
    
    // Legacy getters/setters for backwards compatibility
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public Integer getFoodNumber() { return foodNumber; }
    public void setFoodNumber(Integer foodNumber) { this.foodNumber = foodNumber; }
    
    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }
    
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { 
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        this.rating = rating;
        this.reaction = deriveReactionFromRating(rating);
    }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getReaction() { return reaction; }
    public void setReaction(String reaction) { this.reaction = reaction; }
    
    public Boolean getWasFirstTime() { return wasFirstTime; }
    public void setWasFirstTime(Boolean wasFirstTime) { this.wasFirstTime = wasFirstTime; }
    
    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }
    
    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }
    
    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }
    
    public String getPortion() { return portion; }
    public void setPortion(String portion) { this.portion = portion; }
    
    public String getPreparationMethod() { return preparationMethod; }
    public void setPreparationMethod(String preparationMethod) { this.preparationMethod = preparationMethod; }
    
    public String getAccompaniedBy() { return accompaniedBy; }
    public void setAccompaniedBy(String accompaniedBy) { this.accompaniedBy = accompaniedBy; }
    
    public Boolean getWillTryAgain() { return willTryAgain; }
    public void setWillTryAgain(Boolean willTryAgain) { this.willTryAgain = willTryAgain; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    
    /**
     * Updates the updatedAt timestamp - call before saving.
     */
    public void touch() {
        this.updatedAt = Instant.now();
    }
    
    /**
     * Checks if this is a positive experience (rating 4 or 5).
     */
    public boolean isPositive() {
        return rating != null && rating >= 4;
    }
    
    /**
     * Gets a human-readable description of the rating.
     */
    public String getRatingDescription() {
        if (rating == null) return "Not rated";
        switch (rating) {
            case 1: return "Didn't like it";
            case 2: return "Not very interested";
            case 3: return "It was okay";
            case 4: return "Liked it!";
            case 5: return "Loved it!";
            default: return "Unknown rating";
        }
    }
    
    /**
     * Derives reaction from rating if not explicitly set.
     */
    private String deriveReactionFromRating(Integer rating) {
        if (rating == null) return "neutral";
        if (rating >= 4) return "liked";
        if (rating <= 2) return "disliked";
        return "neutral";
    }
    
    @Override
    public String toString() {
        return "FoodExperience{" +
                "child=" + (child != null ? child.getName() : "null") +
                ", food=" + (food != null ? food.getName() : "null") +
                ", rating=" + rating +
                ", reaction='" + reaction + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}