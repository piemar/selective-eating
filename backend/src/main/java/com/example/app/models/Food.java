package com.example.app.models;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;
import java.util.List;

@Document("foods")
public class Food {
    @Id
    private String id;

    @Indexed
    private String name;
    
    private String description;
    private String imageUrl;
    private List<String> tags; // e.g., ["Sweet", "Soft", "Yellow"]
    private List<String> categories; // e.g., ["Fruits", "Vegetables", "Grains"]
    private String nutritionInfo;
    private Boolean isCommonAllergen = false;
    private List<String> allergens; // e.g., ["Nuts", "Dairy", "Gluten"]
    
    // For AI recommendations
    private List<String> textureProperties; // e.g., ["Crunchy", "Soft", "Chewy"]
    private List<String> flavorProperties; // e.g., ["Sweet", "Savory", "Mild", "Strong"]
    private List<String> visualProperties; // e.g., ["Colorful", "Plain", "Round", "Long"]
    
    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    // Default constructor
    public Food() {
    }

    // Constructor with essential fields
    public Food(String name, String description, String imageUrl, List<String> tags) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.tags = tags;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getNutritionInfo() {
        return nutritionInfo;
    }

    public void setNutritionInfo(String nutritionInfo) {
        this.nutritionInfo = nutritionInfo;
    }

    public Boolean getIsCommonAllergen() {
        return isCommonAllergen;
    }

    public void setIsCommonAllergen(Boolean isCommonAllergen) {
        this.isCommonAllergen = isCommonAllergen;
    }

    public List<String> getAllergens() {
        return allergens;
    }

    public void setAllergens(List<String> allergens) {
        this.allergens = allergens;
    }

    public List<String> getTextureProperties() {
        return textureProperties;
    }

    public void setTextureProperties(List<String> textureProperties) {
        this.textureProperties = textureProperties;
    }

    public List<String> getFlavorProperties() {
        return flavorProperties;
    }

    public void setFlavorProperties(List<String> flavorProperties) {
        this.flavorProperties = flavorProperties;
    }

    public List<String> getVisualProperties() {
        return visualProperties;
    }

    public void setVisualProperties(List<String> visualProperties) {
        this.visualProperties = visualProperties;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
