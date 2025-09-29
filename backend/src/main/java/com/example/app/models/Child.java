package com.example.app.models;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Document("children")
public class Child {
    @Id
    private String id;

    private String name;
    private LocalDate birthDate;
    private String profileImageUrl;
    
    // Link to parent user
    @DBRef
    private User parent;
    
    // Food preferences and restrictions
    private List<String> likedFoodIds;
    private List<String> dislikedFoodIds;
    private List<String> allergens;
    private List<String> dietaryRestrictions; // e.g., "vegetarian", "gluten-free"
    
    // Eating preferences and sensitivities
    private List<String> preferredTextures; // e.g., ["soft", "crunchy"]
    private List<String> preferredFlavors; // e.g., ["sweet", "mild"]
    private List<String> avoidedTextures;
    private List<String> sensitivities; // e.g., ["strong-smells", "mixed-textures"]
    
    // Progress tracking
    private Integer totalFoodsTried = 0;
    private Integer newFavoritesCount = 0;
    private Integer currentStreak = 0; // days of trying new foods
    private Integer longestStreak = 0;
    private Double explorationProgress = 0.0; // percentage
    
    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    // Default constructor
    public Child() {
    }

    // Constructor with essential fields
    public Child(String name, LocalDate birthDate, User parent) {
        this.name = name;
        this.birthDate = birthDate;
        this.parent = parent;
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

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public User getParent() {
        return parent;
    }

    public void setParent(User parent) {
        this.parent = parent;
    }

    public List<String> getLikedFoodIds() {
        return likedFoodIds;
    }

    public void setLikedFoodIds(List<String> likedFoodIds) {
        this.likedFoodIds = likedFoodIds;
    }

    public List<String> getDislikedFoodIds() {
        return dislikedFoodIds;
    }

    public void setDislikedFoodIds(List<String> dislikedFoodIds) {
        this.dislikedFoodIds = dislikedFoodIds;
    }

    public List<String> getAllergens() {
        return allergens;
    }

    public void setAllergens(List<String> allergens) {
        this.allergens = allergens;
    }

    public List<String> getDietaryRestrictions() {
        return dietaryRestrictions;
    }

    public void setDietaryRestrictions(List<String> dietaryRestrictions) {
        this.dietaryRestrictions = dietaryRestrictions;
    }

    public List<String> getPreferredTextures() {
        return preferredTextures;
    }

    public void setPreferredTextures(List<String> preferredTextures) {
        this.preferredTextures = preferredTextures;
    }

    public List<String> getPreferredFlavors() {
        return preferredFlavors;
    }

    public void setPreferredFlavors(List<String> preferredFlavors) {
        this.preferredFlavors = preferredFlavors;
    }

    public List<String> getAvoidedTextures() {
        return avoidedTextures;
    }

    public void setAvoidedTextures(List<String> avoidedTextures) {
        this.avoidedTextures = avoidedTextures;
    }

    public List<String> getSensitivities() {
        return sensitivities;
    }

    public void setSensitivities(List<String> sensitivities) {
        this.sensitivities = sensitivities;
    }

    public Integer getTotalFoodsTried() {
        return totalFoodsTried;
    }

    public void setTotalFoodsTried(Integer totalFoodsTried) {
        this.totalFoodsTried = totalFoodsTried;
    }

    public Integer getNewFavoritesCount() {
        return newFavoritesCount;
    }

    public void setNewFavoritesCount(Integer newFavoritesCount) {
        this.newFavoritesCount = newFavoritesCount;
    }

    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Integer getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(Integer longestStreak) {
        this.longestStreak = longestStreak;
    }

    public Double getExplorationProgress() {
        return explorationProgress;
    }

    public void setExplorationProgress(Double explorationProgress) {
        this.explorationProgress = explorationProgress;
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
