package com.example.app.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.List;

/**
 * Food model with embedded regulatory codes structure.
 * 
 * This implements your brilliant idea where each classification contains
 * its regulatory codes embedded directly, making API access much cleaner.
 * 
 * Collection: foods_embedded_codes
 */
@Document("foods_embedded_codes")
public class FoodEmbeddedCodes {
    
    @Id
    private String id;
    
    
    @Indexed
    private Integer foodNumber;           // Food number as stored in MongoDB
    
    private String name;                  // Food name as stored in MongoDB
    
    @JsonProperty("alt_name")
    private String altName;           // Alternative name (scientific or simplified)
    
    private String language;          // "en" or "sv" 
    
    @JsonProperty("scientific_name")
    private String scientificName;    // Scientific name (e.g., "Bos taurus")
    
    @JsonProperty("food_type")
    private String foodType;          // Data methodology (e.g., "Analysed")
    private String imageUrl;          // Image URL for the food (e.g., "image/foods/1_Bos_taurus.jpg")
    
    @Indexed
    private String foodCategory;      // Primary category (parent level)
    
    // Embedded classifications - each contains name + regulatory codes
    private FoodClassificationsEmbedded classifications;
    
    // CRITICAL MISSING DATA - Raw materials/ingredients
    private List<RawMaterial> rawMaterials;  // Ingredients used to make this food
    
    // Default constructor
    public FoodEmbeddedCodes() {}
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Integer getFoodNumber() { return foodNumber; }
    public void setFoodNumber(Integer foodNumber) { this.foodNumber = foodNumber; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    
    
    
    public String getAltName() { return altName; }
    public void setAltName(String altName) { this.altName = altName; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    
    public String getScientificName() { return scientificName; }
    public void setScientificName(String scientificName) { this.scientificName = scientificName; }
    
    public String getFoodType() { return foodType; }
    public void setFoodType(String foodType) { this.foodType = foodType; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getFoodCategory() { return foodCategory; }
    public void setFoodCategory(String foodCategory) { this.foodCategory = foodCategory; }
    
    public FoodClassificationsEmbedded getClassifications() { return classifications; }
    public void setClassifications(FoodClassificationsEmbedded classifications) { this.classifications = classifications; }
    
    public List<RawMaterial> getRawMaterials() { return rawMaterials; }
    public void setRawMaterials(List<RawMaterial> rawMaterials) { this.rawMaterials = rawMaterials; }
    
    // Utility methods for easy access
    
    /**
     * Get LanguaL ID for any classification type.
     * Safe for Swedish data that may not have classifications.
     */
    public String getLanguaLId(String classificationType) {
        if (classifications != null) {
            try {
                ClassificationWithCodes classification = classifications.getClassification(classificationType);
                return classification != null ? classification.getLangualId() : null;
            } catch (Exception e) {
                // Swedish data may not have this structure
                return null;
            }
        }
        return null;
    }
    
    /**
     * Get facet codes for any classification type.
     * Safe for Swedish data that may not have classifications.
     */
    public String getFacetCodes(String classificationType) {
        if (classifications != null) {
            try {
                ClassificationWithCodes classification = classifications.getClassification(classificationType);
                return classification != null ? classification.getFacetCodes() : null;
            } catch (Exception e) {
                // Swedish data may not have this structure
                return null;
            }
        }
        return null;
    }
    
    /**
     * Get FoodEx2 code directly.
     */
    public String getFoodEx2Code() {
        return classifications != null ? classifications.getFoodex2() : null;
    }
    
    /**
     * Check if this food has animal origin.
     */
    public boolean isAnimalBased() {
        if (classifications != null && classifications.getFoodSource() != null) {
            String source = classifications.getFoodSource().getName();
            return source != null && 
                   (source.toLowerCase().contains("cattle") || 
                    source.toLowerCase().contains("swine") ||
                    source.toLowerCase().contains("animal"));
        }
        return false;
    }
    
    /**
     * Check if this food is plant-based.
     */
    public boolean isPlantBased() {
        if (classifications != null && classifications.getFoodSource() != null) {
            String source = classifications.getFoodSource().getName();
            return source != null && 
                   (source.toLowerCase().contains("plant") || 
                    source.toLowerCase().contains("vegetable") ||
                    source.toLowerCase().contains("fruit") ||
                    source.toLowerCase().contains("grain"));
        }
        return false;
    }
    
    // RAW MATERIALS UTILITY METHODS
    
    /**
     * Get the primary raw material (highest percentage).
     */
    public RawMaterial getPrimaryRawMaterial() {
        if (rawMaterials == null || rawMaterials.isEmpty()) {
            return null;
        }
        
        return rawMaterials.stream()
                .filter(rm -> rm.getPercentage() != null)
                .max((rm1, rm2) -> Double.compare(rm1.getPercentage(), rm2.getPercentage()))
                .orElse(rawMaterials.get(0));
    }
    
    /**
     * Get raw materials that require cooking.
     */
    public List<RawMaterial> getCookedRawMaterials() {
        if (rawMaterials == null) return new java.util.ArrayList<>();
        
        return rawMaterials.stream()
                .filter(RawMaterial::requiresCooking)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Get total percentage of all raw materials (should be ~100%).
     */
    public double getTotalRawMaterialPercentage() {
        if (rawMaterials == null) return 0.0;
        
        return rawMaterials.stream()
                .filter(rm -> rm.getPercentage() != null)
                .mapToDouble(RawMaterial::getPercentage)
                .sum();
    }
    
    /**
     * Get raw materials above a certain percentage threshold.
     */
    public List<RawMaterial> getMajorRawMaterials(double threshold) {
        if (rawMaterials == null) return new java.util.ArrayList<>();
        
        return rawMaterials.stream()
                .filter(rm -> rm.getPercentage() != null && rm.getPercentage() >= threshold)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Get ingredient list as formatted string for display.
     * Safe for Swedish data that may not have rawMaterials.
     */
    public String getIngredientList() {
        if (rawMaterials == null || rawMaterials.isEmpty()) {
            return "No ingredients available";
        }
        
        try {
            return rawMaterials.stream()
                    .sorted((rm1, rm2) -> {
                        // Sort by percentage (descending)
                        if (rm1.getPercentage() != null && rm2.getPercentage() != null) {
                            return Double.compare(rm2.getPercentage(), rm1.getPercentage());
                        }
                        return 0;
                    })
                    .map(RawMaterial::getDisplayName)
                    .collect(java.util.stream.Collectors.joining(", "));
        } catch (Exception e) {
            // Safe fallback for Swedish data
            return "Ingredients data not available in this format";
        }
    }
    
    /**
     * Check if this food has complete raw materials data.
     * Safe for Swedish data that may not have rawMaterials.
     */
    public boolean hasCompleteRawMaterials() {
        if (rawMaterials == null || rawMaterials.isEmpty()) {
            return false;
        }
        
        try {
            return rawMaterials.stream().allMatch(RawMaterial::isComplete);
        } catch (Exception e) {
            // Swedish data may not have this structure
            return false;
        }
    }
    
    @Override
    public String toString() {
        return "FoodEmbeddedCodes{" +
                "foodNumber=" + foodNumber +
                ", name='" + name + '\'' +
                ", foodCategory='" + foodCategory + '\'' +
                ", language='" + language + '\'' +
                '}';
    }
}
