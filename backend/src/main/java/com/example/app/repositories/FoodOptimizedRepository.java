package com.example.app.repositories;

import com.example.app.models.FoodOptimized;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for the optimized food collection with enhanced classification preservation.
 * 
 * This repository provides efficient queries on the foods_optimized_enhanced collection
 * which has 98.3% smaller documents while preserving all compliance codes.
 */
@Repository
public interface FoodOptimizedRepository extends MongoRepository<FoodOptimized, String> {
    
    // ===== BASIC FOOD QUERIES =====
    
    /**
     * Find food by nummer (unique ID) and language.
     */
    Optional<FoodOptimized> findByNummerAndLanguage(Integer nummer, String language);
    
    /**
     * Find all foods by nummer (both Swedish and English).
     */
    List<FoodOptimized> findByNummer(Integer nummer);
    
    /**
     * Find foods by language.
     */
    List<FoodOptimized> findByLanguage(String language);
    
    /**
     * Find foods by name (contains, case insensitive).
     */
    List<FoodOptimized> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find foods with images.
     */
    @Query("{'imageUrl': {$exists: true, $ne: ''}}")
    List<FoodOptimized> findFoodsWithImages();
    
    // ===== CATEGORY QUERIES (ENHANCED) =====
    
    /**
     * Find foods by food category (moved to parent level for better performance).
     */
    @Query("{'foodCategory': ?0}")
    List<FoodOptimized> findByFoodCategory(String foodCategory);
    
    /**
     * Find foods by food category (contains, case insensitive).
     */
    @Query("{'foodCategory': {$regex: ?0, $options: 'i'}}")
    List<FoodOptimized> findByFoodCategoryContaining(String foodCategory);
    
    /**
     * Find foods by product type (simplified structure).
     */
    @Query("{'classifications.product_type': {$regex: ?0, $options: 'i'}}")
    List<FoodOptimized> findByProductType(String productType);
    
    /**
     * Find foods by food source (e.g., "Cattle", "Plant").
     */
    @Query("{'classifications.food_source': {$regex: ?0, $options: 'i'}}")
    List<FoodOptimized> findByFoodSource(String foodSource);
    
    /**
     * Find animal products.
     */
    @Query("{'classifications.food_source': {$regex: 'cattle|swine|animal', $options: 'i'}}")
    List<FoodOptimized> findAnimalProducts();
    
    /**
     * Find plant-based products.
     */
    @Query("{'classifications.food_source': {$regex: 'plant|vegetable|fruit|grain', $options: 'i'}}")
    List<FoodOptimized> findPlantBasedProducts();
    
    // ===== CLASSIFICATION QUERIES (UPDATED STRUCTURE) =====
    
    /**
     * Find foods by physical state (new field available).
     */
    @Query("{'classifications.physical_state': {$regex: ?0, $options: 'i'}}")
    List<FoodOptimized> findByPhysicalState(String physicalState);
    
    /**
     * Find foods by heat treatment requirement.
     */
    @Query("{'classifications.heat_treatment': {$regex: ?0, $options: 'i'}}")
    List<FoodOptimized> findByHeatTreatment(String heatTreatment);
    
    /**
     * Find foods by preservation method.
     */
    @Query("{'classifications.preservation': {$regex: ?0, $options: 'i'}}")
    List<FoodOptimized> findByPreservation(String preservation);
    
    /**
     * Find foods by geographic origin.
     */
    @Query("{'classifications.geographic_origin': {$regex: ?0, $options: 'i'}}")
    List<FoodOptimized> findByGeographicOrigin(String origin);
    
    // ===== ADVANCED QUERIES =====
    
    /**
     * Find foods by multiple food categories.
     */
    @Query("{'foodCategory': {$in: ?0}}")
    List<FoodOptimized> findByFoodCategoriesIn(List<String> foodCategories);
    
    /**
     * Find foods by scientific name.
     */
    List<FoodOptimized> findByScientificNameContainingIgnoreCase(String scientificName);
    
    /**
     * Find foods with USDA matches.
     */
    List<FoodOptimized> findByMatched(Boolean matched);
    
    /**
     * Find foods by food type (e.g., "Analysed").
     */
    List<FoodOptimized> findByFoodType(String foodType);
    
    // ===== PAGINATION SUPPORT =====
    
    /**
     * Find foods by main category with pagination.
     */
    @Query("{'categories.main': {$regex: ?0, $options: 'i'}}")
    Page<FoodOptimized> findByMainCategoryContaining(String mainCategory, Pageable pageable);
    
    /**
     * Find foods by language with pagination.
     */
    Page<FoodOptimized> findByLanguage(String language, Pageable pageable);
    
    /**
     * Find foods with images with pagination.
     */
    @Query("{'imageUrl': {$exists: true, $ne: ''}}")
    Page<FoodOptimized> findFoodsWithImages(Pageable pageable);
    
    // ===== COUNT QUERIES =====
    
    /**
     * Count foods by main category.
     */
    @Query(value = "{'categories.main': ?0}", count = true)
    Long countByMainCategory(String mainCategory);
    
    /**
     * Count foods by language.
     */
    Long countByLanguage(String language);
    
    /**
     * Count foods with images.
     */
    @Query(value = "{'imageUrl': {$exists: true, $ne: ''}}", count = true)
    Long countFoodsWithImages();
    
    // ===== UTILITY QUERIES =====
    
    /**
     * Get all distinct main categories.
     */
    @Query(value = "{}", fields = "{'categories.main': 1}")
    List<FoodOptimized> findDistinctMainCategories();
    
    /**
     * Find foods for image generation (English only, no image yet).
     */
    @Query("{'language': 'en', 'imageUrl': {$exists: false}}")
    List<FoodOptimized> findFoodsNeedingImages();
    
    /**
     * Find foods by nummer range (for batch processing).
     */
    List<FoodOptimized> findByNummerBetween(Integer startNummer, Integer endNummer);
}
