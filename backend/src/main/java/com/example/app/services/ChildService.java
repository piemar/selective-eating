package com.example.app.services;

import com.example.app.models.Child;
import com.example.app.models.User;
import com.example.app.repositories.ChildRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChildService {

    private final ChildRepository childRepository;

    @Autowired
    public ChildService(ChildRepository childRepository) {
        this.childRepository = childRepository;
    }

    // Create operations
    public Child createChild(Child child) {
        // Validate that parent exists
        if (child.getParent() == null) {
            throw new RuntimeException("Child must have a parent assigned");
        }

        // Check if child with same name already exists for this parent
        Optional<Child> existingChild = childRepository.findByNameAndParent(child.getName(), child.getParent());
        if (existingChild.isPresent()) {
            throw new RuntimeException("Child with name '" + child.getName() + "' already exists for this parent");
        }

        // Initialize progress tracking fields if not set
        if (child.getTotalFoodsTried() == null) child.setTotalFoodsTried(0);
        if (child.getNewFavoritesCount() == null) child.setNewFavoritesCount(0);
        if (child.getCurrentStreak() == null) child.setCurrentStreak(0);
        if (child.getLongestStreak() == null) child.setLongestStreak(0);
        if (child.getExplorationProgress() == null) child.setExplorationProgress(0.0);

        return childRepository.save(child);
    }

    // Read operations
    public List<Child> getAllChildren() {
        return childRepository.findAll();
    }

    public Optional<Child> getChildById(String id) {
        return childRepository.findById(id);
    }

    public List<Child> getChildrenByParent(User parent) {
        return childRepository.findByParent(parent);
    }

    public List<Child> getChildrenByParentId(String parentId) {
        return childRepository.findByParentId(parentId);
    }

    public Optional<Child> getChildByNameAndParent(String name, User parent) {
        return childRepository.findByNameAndParent(name, parent);
    }

    // Progress tracking methods
    public Child updateFoodsTried(String childId, int increment) {
        Child child = childRepository.findById(childId)
            .orElseThrow(() -> new RuntimeException("Child not found with id: " + childId));
        
        child.setTotalFoodsTried(child.getTotalFoodsTried() + increment);
        return childRepository.save(child);
    }

    public Child updateFavoritesCount(String childId, int increment) {
        Child child = childRepository.findById(childId)
            .orElseThrow(() -> new RuntimeException("Child not found with id: " + childId));
        
        child.setNewFavoritesCount(child.getNewFavoritesCount() + increment);
        return childRepository.save(child);
    }

    public Child updateStreak(String childId, int newStreak) {
        Child child = childRepository.findById(childId)
            .orElseThrow(() -> new RuntimeException("Child not found with id: " + childId));
        
        child.setCurrentStreak(newStreak);
        if (newStreak > child.getLongestStreak()) {
            child.setLongestStreak(newStreak);
        }
        return childRepository.save(child);
    }

    public Child updateExplorationProgress(String childId, double progress) {
        Child child = childRepository.findById(childId)
            .orElseThrow(() -> new RuntimeException("Child not found with id: " + childId));
        
        child.setExplorationProgress(Math.max(0.0, Math.min(100.0, progress))); // Clamp between 0-100%
        return childRepository.save(child);
    }

    // Food preference management
    public Child addLikedFood(String childId, String foodId) {
        Child child = childRepository.findById(childId)
            .orElseThrow(() -> new RuntimeException("Child not found with id: " + childId));
        
        if (child.getLikedFoodIds() == null) {
            child.setLikedFoodIds(List.of(foodId));
        } else if (!child.getLikedFoodIds().contains(foodId)) {
            child.getLikedFoodIds().add(foodId);
        }
        
        // Remove from disliked if it was there
        if (child.getDislikedFoodIds() != null && child.getDislikedFoodIds().contains(foodId)) {
            child.getDislikedFoodIds().remove(foodId);
        }
        
        return childRepository.save(child);
    }

    public Child addDislikedFood(String childId, String foodId) {
        Child child = childRepository.findById(childId)
            .orElseThrow(() -> new RuntimeException("Child not found with id: " + childId));
        
        if (child.getDislikedFoodIds() == null) {
            child.setDislikedFoodIds(List.of(foodId));
        } else if (!child.getDislikedFoodIds().contains(foodId)) {
            child.getDislikedFoodIds().add(foodId);
        }
        
        // Remove from liked if it was there
        if (child.getLikedFoodIds() != null && child.getLikedFoodIds().contains(foodId)) {
            child.getLikedFoodIds().remove(foodId);
        }
        
        return childRepository.save(child);
    }

    public Child removeFoodPreference(String childId, String foodId) {
        Child child = childRepository.findById(childId)
            .orElseThrow(() -> new RuntimeException("Child not found with id: " + childId));
        
        if (child.getLikedFoodIds() != null) {
            child.getLikedFoodIds().remove(foodId);
        }
        if (child.getDislikedFoodIds() != null) {
            child.getDislikedFoodIds().remove(foodId);
        }
        
        return childRepository.save(child);
    }

    // Dietary and preference queries
    public List<Child> getChildrenWithDietaryRestriction(String restriction) {
        return childRepository.findByDietaryRestrictionsContaining(restriction);
    }

    public List<Child> getChildrenWithAllergen(String allergen) {
        return childRepository.findByAllergensContaining(allergen);
    }

    public List<Child> getActiveChildren() {
        return childRepository.findActiveChildren();
    }

    public List<Child> getHighProgressChildren(double minProgress) {
        return childRepository.findByExplorationProgressGreaterThanEqual(minProgress);
    }

    // Update operations
    public Child updateChild(String id, Child childDetails) {
        Child child = childRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Child not found with id: " + id));

        // Update allowed fields
        if (childDetails.getName() != null) {
            child.setName(childDetails.getName());
        }
        if (childDetails.getBirthDate() != null) {
            child.setBirthDate(childDetails.getBirthDate());
        }
        if (childDetails.getProfileImageUrl() != null) {
            child.setProfileImageUrl(childDetails.getProfileImageUrl());
        }
        if (childDetails.getAllergens() != null) {
            child.setAllergens(childDetails.getAllergens());
        }
        if (childDetails.getDietaryRestrictions() != null) {
            child.setDietaryRestrictions(childDetails.getDietaryRestrictions());
        }
        if (childDetails.getPreferredTextures() != null) {
            child.setPreferredTextures(childDetails.getPreferredTextures());
        }
        if (childDetails.getPreferredFlavors() != null) {
            child.setPreferredFlavors(childDetails.getPreferredFlavors());
        }
        if (childDetails.getAvoidedTextures() != null) {
            child.setAvoidedTextures(childDetails.getAvoidedTextures());
        }
        if (childDetails.getSensitivities() != null) {
            child.setSensitivities(childDetails.getSensitivities());
        }

        return childRepository.save(child);
    }

    // Delete operations
    public void deleteChild(String id) {
        if (!childRepository.existsById(id)) {
            throw new RuntimeException("Child not found with id: " + id);
        }
        childRepository.deleteById(id);
    }

    // Statistics methods
    public long getChildCount() {
        return childRepository.count();
    }

    public long getChildCountByParent(User parent) {
        return childRepository.countByParent(parent);
    }

    // Helper methods
    public Child saveChild(Child child) {
        return childRepository.save(child);
    }
}
