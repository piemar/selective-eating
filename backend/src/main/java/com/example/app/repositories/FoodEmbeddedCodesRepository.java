package com.example.app.repositories;

import com.example.app.models.FoodEmbeddedCodes;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for FoodEmbeddedCodes - the brilliant embedded structure!
 * 
 * Each query can directly access embedded classification codes without 
 * searching through arrays or complex structures.
 */
@Repository
public interface FoodEmbeddedCodesRepository extends MongoRepository<FoodEmbeddedCodes, String> {
    
    // Basic queries
    Optional<FoodEmbeddedCodes> findByFoodNumber(Integer foodNumber);
    List<FoodEmbeddedCodes> findByLanguage(String language);
    List<FoodEmbeddedCodes> findAllByFoodNumberIn(List<Integer> foodNumbers);
    
    // Name search is now handled in the service layer for Swedish field compatibility
    
    // Food category queries (parent level - super fast!)
    @Query("{'foodCategory': ?0}")
    List<FoodEmbeddedCodes> findByFoodCategory(String foodCategory);
    
    @Query("{'foodCategory': {$in: ?0}}")
    List<FoodEmbeddedCodes> findByFoodCategoriesIn(List<String> categories);
    
    // Direct embedded classification queries - YOUR BRILLIANT STRUCTURE!
    
    // Product type queries
    @Query("{'classifications.product_type.name': {$regex: ?0, $options: 'i'}}")
    List<FoodEmbeddedCodes> findByProductTypeName(String productTypeName);
    
    @Query("{'classifications.product_type.langual_id': ?0}")
    List<FoodEmbeddedCodes> findByProductTypeLangualId(String langualId);
    
    // Food source queries
    @Query("{'classifications.food_source.name': {$regex: ?0, $options: 'i'}}")
    List<FoodEmbeddedCodes> findByFoodSourceName(String foodSourceName);
    
    @Query("{'classifications.food_source.langual_id': ?0}")
    List<FoodEmbeddedCodes> findByFoodSourceLangualId(String langualId);
    
    // Animal vs Plant queries
    @Query("{'classifications.food_source.name': {$regex: 'cattle|swine|animal', $options: 'i'}}")
    List<FoodEmbeddedCodes> findAnimalProducts();
    
    @Query("{'classifications.food_source.name': {$regex: 'plant|vegetable|fruit|grain', $options: 'i'}}")
    List<FoodEmbeddedCodes> findPlantBasedProducts();
    
    // Physical state queries
    @Query("{'classifications.physical_state.name': {$regex: ?0, $options: 'i'}}")
    List<FoodEmbeddedCodes> findByPhysicalStateName(String physicalState);
    
    @Query("{'classifications.physical_state.facet_codes': {$regex: ?0, $options: 'i'}}")
    List<FoodEmbeddedCodes> findByPhysicalStateFacetCodes(String facetCodes);
    
    // Heat treatment queries
    @Query("{'classifications.heat_treatment.name': {$regex: ?0, $options: 'i'}}")
    List<FoodEmbeddedCodes> findByHeatTreatmentName(String heatTreatment);
    
    // Preservation queries
    @Query("{'classifications.preservation.name': {$regex: ?0, $options: 'i'}}")
    List<FoodEmbeddedCodes> findByPreservationName(String preservation);
    
    // Geographic origin queries
    @Query("{'classifications.geographic_origin.name': {$regex: ?0, $options: 'i'}}")
    List<FoodEmbeddedCodes> findByGeographicOriginName(String origin);
    
    // FoodEx2 queries
    @Query("{'classifications.foodex2': ?0}")
    List<FoodEmbeddedCodes> findByFoodEx2Code(String foodex2Code);
    
    @Query("{'classifications.foodex2': {$regex: ?0, $options: 'i'}}")
    List<FoodEmbeddedCodes> findByFoodEx2CodePattern(String pattern);
    
    // Advanced embedded queries - THE BEAUTY OF YOUR STRUCTURE!
    
    /**
     * Find foods by specific LanguaL ID in ANY classification.
     * This is much cleaner than searching arrays!
     */
    @Query("{'$or': [" +
           "{'classifications.product_type.langual_id': ?0}," +
           "{'classifications.food_source.langual_id': ?0}," +
           "{'classifications.part_used.langual_id': ?0}," +
           "{'classifications.physical_state.langual_id': ?0}," +
           "{'classifications.heat_treatment.langual_id': ?0}," +
           "{'classifications.preservation.langual_id': ?0}," +
           "{'classifications.packing_medium.langual_id': ?0}," +
           "{'classifications.consumer_group.langual_id': ?0}," +
           "{'classifications.geographic_origin.langual_id': ?0}" +
           "]}")
    List<FoodEmbeddedCodes> findByAnyLangualId(String langualId);
    
    /**
     * Find foods that match specific classification criteria.
     * Example: Animal products that are chilled and semisolid
     */
    @Query("{'classifications.food_source.name': {$regex: 'animal|cattle|swine', $options: 'i'}, " +
           "'classifications.preservation.name': {$regex: 'chilled', $options: 'i'}, " +
           "'classifications.physical_state.name': {$regex: 'semisolid', $options: 'i'}}")
    List<FoodEmbeddedCodes> findAnimalChilledSemisolid();
    
    /**
     * Complex compliance query - find foods by multiple regulatory codes.
     */
    @Query("{'classifications.product_type.facet_codes': {$regex: ?0}, " +
           "'classifications.food_source.langual_id': ?1}")
    List<FoodEmbeddedCodes> findByProductFacetCodesAndFoodSourceLangualId(String facetCodes, String langualId);
    
    // Count queries for statistics
    @Query(value = "{'classifications.product_type.langual_id': {$exists: true}}", count = true)
    long countWithProductTypeLangualId();
    
    @Query(value = "{'classifications.foodex2': {$exists: true}}", count = true)
    long countWithFoodEx2();
    
    // Performance-optimized unique queries
    @Query("{'foodNumber': ?0, 'language': 'en'}")
    Optional<FoodEmbeddedCodes> findEnglishByFoodNumber(Integer foodNumber);
    
    @Query("{'foodNumber': ?0, 'language': 'sv'}")
    Optional<FoodEmbeddedCodes> findSwedishByFoodNumber(Integer foodNumber);
}
