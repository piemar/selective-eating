package com.example.app.repositories;

import com.example.app.models.Food;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodRepository extends MongoRepository<Food, String> {
    
    Optional<Food> findByName(String name);
    List<Food> findByNameContainingIgnoreCase(String name);
    
    // Find foods by category
    List<Food> findByCategoriesContaining(String category);
    
    // Find foods by tags
    List<Food> findByTagsContaining(String tag);
    Page<Food> findByTagsContaining(String tag, Pageable pageable);
    
    // Find foods with multiple tags
    @Query("{'tags': {'$in': ?0}}")
    List<Food> findByTagsIn(List<String> tags);
    
    // Find foods by texture properties (for AI recommendations)
    List<Food> findByTexturePropertiesContaining(String textureProperty);
    
    // Find foods by flavor properties (for AI recommendations)
    List<Food> findByFlavorPropertiesContaining(String flavorProperty);
    
    // Find foods by visual properties
    List<Food> findByVisualPropertiesContaining(String visualProperty);
    
    // Complex query for AI suggestions - foods with similar properties
    @Query("{'$or': [" +
           "{'textureProperties': {'$in': ?0}}, " +
           "{'flavorProperties': {'$in': ?1}}, " +
           "{'visualProperties': {'$in': ?2}}" +
           "]}")
    List<Food> findSimilarFoods(List<String> textureProperties, 
                               List<String> flavorProperties, 
                               List<String> visualProperties);
    
    // Find foods excluding allergens
    @Query("{'allergens': {'$not': {'$in': ?0}}}")
    List<Food> findFoodsWithoutAllergens(List<String> allergens);
    
    // Find foods by category with allergen exclusion
    @Query("{'categories': {'$in': ?0}, 'allergens': {'$not': {'$in': ?1}}}")
    List<Food> findByCategoriesInAndAllergensNotIn(List<String> categories, List<String> allergens);
    
    // Count foods by category
    Long countByCategories(String category);
    
    // Find foods that are not common allergens
    List<Food> findByIsCommonAllergen(Boolean isCommonAllergen);
}
