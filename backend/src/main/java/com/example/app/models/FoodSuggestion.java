package com.example.app.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.List;

/**
 * Represents an AI-generated food suggestion based on user preferences.
 */
@Document("food_suggestions")
public class FoodSuggestion {
    
    @Id
    private String id;
    
    private Integer foodNumber;          // Reference to the suggested food
    private String foodName;             // Name of the suggested food
    private String imageUrl;             // Image URL for the suggested food
    private List<String> tags;           // Tags like "Sweet", "Crunchy", etc.
    private String reason;               // AI explanation for why this food is suggested
    private Double confidenceScore;      // AI confidence in this suggestion (0.0-1.0)
    private List<Integer> basedOnFoods;  // Food numbers this suggestion is based on
    private Instant createdAt;
    
    // Constructors
    public FoodSuggestion() {
        this.createdAt = Instant.now();
    }
    
    public FoodSuggestion(Integer foodNumber, String foodName, String imageUrl, 
                         List<String> tags, String reason, Double confidenceScore, 
                         List<Integer> basedOnFoods) {
        this();
        this.foodNumber = foodNumber;
        this.foodName = foodName;
        this.imageUrl = imageUrl;
        this.tags = tags;
        this.reason = reason;
        this.confidenceScore = confidenceScore;
        this.basedOnFoods = basedOnFoods;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Integer getFoodNumber() { return foodNumber; }
    public void setFoodNumber(Integer foodNumber) { this.foodNumber = foodNumber; }
    
    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }
    
    public List<Integer> getBasedOnFoods() { return basedOnFoods; }
    public void setBasedOnFoods(List<Integer> basedOnFoods) { this.basedOnFoods = basedOnFoods; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    @Override
    public String toString() {
        return "FoodSuggestion{" +
                "foodNumber=" + foodNumber +
                ", foodName='" + foodName + '\'' +
                ", confidenceScore=" + confidenceScore +
                '}';
    }
}
