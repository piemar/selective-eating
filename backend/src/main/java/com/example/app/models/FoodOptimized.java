package com.example.app.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.List;

/**
 * Optimized Food model for the foods_complete_classifications collection.
 * This model maps to the complete classifications documents with 99.1% size reduction.
 * 
 * Original document size: ~144 KB  
 * Optimized document size: ~5.3 KB (complete with all classifications)
 * 
 * Key improvements:
 * - foodCategory at parent level for better queries
 * - Complete classifications with all meaningful regulatory data
 * - Fixed alt_name with same-language alternatives
 * - food_type restored for data methodology tracking
 */
@Document("foods_embedded_codes")
public class FoodOptimized {
    
    @Id
    private String id;
    
    @Indexed
    private Integer nummer; // Unique food identifier from Livsmedelsverket
    
    @Indexed
    private String language; // "en" or "sv"
    
    @Indexed
    private String name; // Food name in the specified language
    
    private String altName; // Alternative name (usually other language)
    
    private String scientificName; // Latin/scientific name (e.g., "Bos taurus")
    
    private String foodType; // Type of food analysis (e.g., "Analysed")
    
    @Indexed
    private String foodCategory; // Primary food category (moved to parent level for better queries)
    
    private FoodClassificationsComplete classifications; // Complete classifications with all meaningful data
    
    private String imageUrl; // Path to generated food image
    
    private Boolean matched; // Whether this food has USDA nutritional matches
    
    // Default constructor
    public FoodOptimized() {
    }
    
    // Constructor with essential fields
    public FoodOptimized(Integer nummer, String language, String name) {
        this.nummer = nummer;
        this.language = language;
        this.name = name;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Integer getNummer() {
        return nummer;
    }
    
    public void setNummer(Integer nummer) {
        this.nummer = nummer;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAltName() {
        return altName;
    }
    
    public void setAltName(String altName) {
        this.altName = altName;
    }
    
    public String getScientificName() {
        return scientificName;
    }
    
    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }
    
    public String getFoodType() {
        return foodType;
    }
    
    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }
    
    public String getFoodCategory() {
        return foodCategory;
    }
    
    public void setFoodCategory(String foodCategory) {
        this.foodCategory = foodCategory;
    }
    
    public FoodClassificationsComplete getClassifications() {
        return classifications;
    }
    
    public void setClassifications(FoodClassificationsComplete classifications) {
        this.classifications = classifications;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public Boolean getMatched() {
        return matched;
    }
    
    public void setMatched(Boolean matched) {
        this.matched = matched;
    }
    
    @Override
    public String toString() {
        return "FoodOptimized{" +
                "id='" + id + '\'' +
                ", nummer=" + nummer +
                ", language='" + language + '\'' +
                ", name='" + name + '\'' +
                ", scientificName='" + scientificName + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
