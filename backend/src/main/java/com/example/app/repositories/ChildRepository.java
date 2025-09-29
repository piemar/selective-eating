package com.example.app.repositories;

import com.example.app.models.Child;
import com.example.app.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChildRepository extends MongoRepository<Child, String> {
    
    // Find children by parent
    List<Child> findByParent(User parent);
    List<Child> findByParentId(String parentId);
    
    // Find child by name and parent
    Optional<Child> findByNameAndParent(String name, User parent);
    
    // Find children with specific dietary restrictions
    List<Child> findByDietaryRestrictionsContaining(String restriction);
    
    // Find children with specific allergens
    List<Child> findByAllergensContaining(String allergen);
    
    // Find children by preferred textures (for matching recommendations)
    List<Child> findByPreferredTexturesContaining(String texture);
    
    // Find children by preferred flavors
    List<Child> findByPreferredFlavorsContaining(String flavor);
    
    // Find active children (those with recent progress)
    @Query("{'currentStreak': {'$gt': 0}}")
    List<Child> findActiveChildren();
    
    // Find children with high exploration progress
    @Query("{'explorationProgress': {'$gte': ?0}}")
    List<Child> findByExplorationProgressGreaterThanEqual(Double minProgress);
    
    // Find children who have tried specific foods
    List<Child> findByLikedFoodIdsContaining(String foodId);
    List<Child> findByDislikedFoodIdsContaining(String foodId);
    
    // Get children statistics
    @Query("{'totalFoodsTried': {'$gte': ?0}}")
    List<Child> findByTotalFoodsTriedGreaterThanEqual(Integer minFoodsTried);
    
    @Query("{'newFavoritesCount': {'$gte': ?0}}")
    List<Child> findByNewFavoritesCountGreaterThanEqual(Integer minFavorites);
    
    // Count children by parent
    Long countByParent(User parent);
}
