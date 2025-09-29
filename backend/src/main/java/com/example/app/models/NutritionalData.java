package com.example.app.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.List;

/**
 * Nutritional data model for the nutritional_data collection.
 * This is extracted from the original verbose documents to improve performance.
 * 
 * Loaded only when nutritional information is actually needed.
 */
@Document("nutritional_data")
public class NutritionalData {
    
    @Id
    private String id;
    
    @Indexed
    private Integer foodNumber; // References FoodEmbeddedCodes.foodNumber
    
    @Indexed
    private String language; // "en" or "sv"
    
    private List<Nutrient> nutrients; // Simplified nutritional data
    
    // Nested class for individual nutrients
    public static class Nutrient {
        private String name; // e.g., "Protein", "Vitamin C"
        private String code; // euroFIR code (e.g., "PROT", "VITC")
        private Double value; // Numerical value
        private String unit; // e.g., "g", "mg", "kcal"
        
        // NEW CRITICAL FIELDS (from analysis of missing fields)
        private String measurementBasis; // e.g., "per 100 g edible portion", "per 100 g total fatty acid"
        private String measurementBasisCode; // e.g., "W" (weight), "F" (fatty acids)
        private Integer portionSizeGrams; // e.g., 100 (reference portion size)
        
        // Default constructor
        public Nutrient() {}
        
        // Constructor with original fields
        public Nutrient(String name, String code, Double value, String unit) {
            this.name = name;
            this.code = code;
            this.value = value;
            this.unit = unit;
        }
        
        // Constructor with all fields including new ones
        public Nutrient(String name, String code, Double value, String unit, 
                       String measurementBasis, String measurementBasisCode, Integer portionSizeGrams) {
            this.name = name;
            this.code = code;
            this.value = value;
            this.unit = unit;
            this.measurementBasis = measurementBasis;
            this.measurementBasisCode = measurementBasisCode;
            this.portionSizeGrams = portionSizeGrams;
        }
        
        // Getters and Setters
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getCode() {
            return code;
        }
        
        public void setCode(String code) {
            this.code = code;
        }
        
        public Double getValue() {
            return value;
        }
        
        public void setValue(Double value) {
            this.value = value;
        }
        
        public String getUnit() {
            return unit;
        }
        
        public void setUnit(String unit) {
            this.unit = unit;
        }
        
        // NEW FIELD GETTERS AND SETTERS
        public String getMeasurementBasis() {
            return measurementBasis;
        }
        
        public void setMeasurementBasis(String measurementBasis) {
            this.measurementBasis = measurementBasis;
        }
        
        public String getMeasurementBasisCode() {
            return measurementBasisCode;
        }
        
        public void setMeasurementBasisCode(String measurementBasisCode) {
            this.measurementBasisCode = measurementBasisCode;
        }
        
        public Integer getPortionSizeGrams() {
            return portionSizeGrams;
        }
        
        public void setPortionSizeGrams(Integer portionSizeGrams) {
            this.portionSizeGrams = portionSizeGrams;
        }
        
        // UTILITY METHODS
        
        /**
         * Check if this nutrient is measured per 100g edible portion (most common).
         */
        public boolean isPerEditablePortion() {
            return "W".equals(measurementBasisCode) || 
                   (measurementBasis != null && measurementBasis.contains("edible portion"));
        }
        
        /**
         * Check if this nutrient is measured per 100g total fatty acids.
         */
        public boolean isPerFattyAcid() {
            return "F".equals(measurementBasisCode) || 
                   (measurementBasis != null && measurementBasis.contains("fatty acid"));
        }
        
        /**
         * Get display string for the measurement basis.
         */
        public String getMeasurementBasisDisplay() {
            if (measurementBasis != null && !measurementBasis.equals("undefined")) {
                return measurementBasis;
            }
            // Fallback based on code
            if ("W".equals(measurementBasisCode)) {
                return "per 100 g edible portion";
            } else if ("F".equals(measurementBasisCode)) {
                return "per 100 g total fatty acid";
            }
            return "per 100 g";
        }
        
        @Override
        public String toString() {
            return "Nutrient{" +
                    "name='" + name + '\'' +
                    ", code='" + code + '\'' +
                    ", value=" + value +
                    ", unit='" + unit + '\'' +
                    ", measurementBasis='" + measurementBasis + '\'' +
                    ", measurementBasisCode='" + measurementBasisCode + '\'' +
                    ", portionSizeGrams=" + portionSizeGrams +
                    '}';
        }
    }
    
    // Default constructor
    public NutritionalData() {}
    
    // Constructor
    public NutritionalData(Integer foodNumber, String language) {
        this.foodNumber = foodNumber;
        this.language = language;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Integer getFoodNumber() {
        return foodNumber;
    }
    
    public void setFoodNumber(Integer foodNumber) {
        this.foodNumber = foodNumber;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public List<Nutrient> getNutrients() {
        return nutrients;
    }
    
    public void setNutrients(List<Nutrient> nutrients) {
        this.nutrients = nutrients;
    }
    
    @Override
    public String toString() {
        return "NutritionalData{" +
                "id='" + id + '\'' +
                ", foodNumber=" + foodNumber +
                ", language='" + language + '\'' +
                ", nutrients=" + (nutrients != null ? nutrients.size() : 0) + " nutrients" +
                '}';
    }
}
