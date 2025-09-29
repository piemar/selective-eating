package com.example.app.models;

import java.util.List;

/**
 * Enhanced food categories that preserve detailed classification codes.
 * 
 * This version maintains compliance codes (fasettkod, langualId) while 
 * still keeping documents optimized and efficient.
 */
public class FoodCategoriesEnhanced {
    
    private String main;                           // Main group category
    private List<LanguaLFacet> langualFacets;     // Key LanguaL facets with codes
    private FoodEx2Code foodex2;                  // FoodEx2 classification
    private List<RegulatoryCode> regulatoryCodes; // Compliance codes
    
    // Nested classes for structured data
    public static class LanguaLFacet {
        private String facet;      // e.g., "A. PRODUCT TYPE"
        private String name;       // e.g., "Animal fats" 
        private String codes;      // e.g., "A0777, A0352"
        private String langualId;  // e.g., "A0810"
        
        // Constructors
        public LanguaLFacet() {}
        
        public LanguaLFacet(String facet, String name, String codes, String langualId) {
            this.facet = facet;
            this.name = name;
            this.codes = codes;
            this.langualId = langualId;
        }
        
        // Getters and Setters
        public String getFacet() { return facet; }
        public void setFacet(String facet) { this.facet = facet; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getCodes() { return codes; }
        public void setCodes(String codes) { this.codes = codes; }
        
        public String getLangualId() { return langualId; }
        public void setLangualId(String langualId) { this.langualId = langualId; }
        
        @Override
        public String toString() {
            return "LanguaLFacet{facet='" + facet + "', name='" + name + 
                   "', codes='" + codes + "', langualId='" + langualId + "'}";
        }
    }
    
    public static class FoodEx2Code {
        private String code;  // e.g., "A037X"
        private String type;  // "FoodEx2"
        
        // Constructors
        public FoodEx2Code() {}
        
        public FoodEx2Code(String code, String type) {
            this.code = code;
            this.type = type;
        }
        
        // Getters and Setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        @Override
        public String toString() {
            return "FoodEx2Code{code='" + code + "', type='" + type + "'}";
        }
    }
    
    public static class RegulatoryCode {
        private String type;       // e.g., "LanguaL"
        private String facetCode;  // e.g., "A0777, A0352"
        private String langualId;  // e.g., "A0810"
        private String facet;      // e.g., "A. PRODUCT TYPE"
        
        // Constructors
        public RegulatoryCode() {}
        
        public RegulatoryCode(String type, String facetCode, String langualId, String facet) {
            this.type = type;
            this.facetCode = facetCode;
            this.langualId = langualId;
            this.facet = facet;
        }
        
        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getFacetCode() { return facetCode; }
        public void setFacetCode(String facetCode) { this.facetCode = facetCode; }
        
        public String getLangualId() { return langualId; }
        public void setLangualId(String langualId) { this.langualId = langualId; }
        
        public String getFacet() { return facet; }
        public void setFacet(String facet) { this.facet = facet; }
        
        @Override
        public String toString() {
            return "RegulatoryCode{type='" + type + "', facetCode='" + facetCode + 
                   "', langualId='" + langualId + "', facet='" + facet + "'}";
        }
    }
    
    // Main class constructors
    public FoodCategoriesEnhanced() {}
    
    public FoodCategoriesEnhanced(String main) {
        this.main = main;
    }
    
    // Main class getters and setters
    public String getMain() { return main; }
    public void setMain(String main) { this.main = main; }
    
    public List<LanguaLFacet> getLangualFacets() { return langualFacets; }
    public void setLangualFacets(List<LanguaLFacet> langualFacets) { this.langualFacets = langualFacets; }
    
    public FoodEx2Code getFoodex2() { return foodex2; }
    public void setFoodex2(FoodEx2Code foodex2) { this.foodex2 = foodex2; }
    
    public List<RegulatoryCode> getRegulatoryCode() { return regulatoryCodes; }
    public void setRegulatoryCode(List<RegulatoryCode> regulatoryCodes) { this.regulatoryCodes = regulatoryCodes; }
    
    // Utility methods
    public String getDisplayName() {
        if (main != null && !main.isEmpty()) {
            return main;
        }
        if (langualFacets != null && !langualFacets.isEmpty()) {
            return langualFacets.get(0).getName();
        }
        return "Unknown category";
    }
    
    /**
     * Get primary product type from LanguaL facets.
     */
    public String getProductType() {
        if (langualFacets != null) {
            for (LanguaLFacet facet : langualFacets) {
                if ("A. PRODUCT TYPE".equals(facet.getFacet())) {
                    return facet.getName();
                }
            }
        }
        return null;
    }
    
    /**
     * Get food source from LanguaL facets.
     */
    public String getFoodSource() {
        if (langualFacets != null) {
            for (LanguaLFacet facet : langualFacets) {
                if ("B. FOOD SOURCE".equals(facet.getFacet())) {
                    return facet.getName();
                }
            }
        }
        return null;
    }
    
    /**
     * Find regulatory code by facet code.
     */
    public RegulatoryCode findRegulatoryCode(String facetCode) {
        if (regulatoryCodes != null) {
            for (RegulatoryCode code : regulatoryCodes) {
                if (code.getFacetCode().contains(facetCode)) {
                    return code;
                }
            }
        }
        return null;
    }
    
    /**
     * Check if this food is in a specific category.
     */
    public boolean isInCategory(String category) {
        if (category == null || category.isEmpty()) {
            return false;
        }
        
        String lowerCategory = category.toLowerCase();
        
        // Check main category
        if (main != null && main.toLowerCase().contains(lowerCategory)) {
            return true;
        }
        
        // Check LanguaL facets
        if (langualFacets != null) {
            for (LanguaLFacet facet : langualFacets) {
                if (facet.getName() != null && facet.getName().toLowerCase().contains(lowerCategory)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Dietary restriction checks.
     */
    public boolean isAnimalProduct() {
        return isInCategory("animal") || isInCategory("cattle") || 
               isInCategory("swine") || isInCategory("meat");
    }
    
    public boolean isPlantBased() {
        return isInCategory("plant") || isInCategory("fruit") || 
               isInCategory("vegetable") || isInCategory("grain");
    }
    
    /**
     * Get all classification codes for compliance reporting.
     */
    public String getComplianceSummary() {
        StringBuilder summary = new StringBuilder();
        
        if (foodex2 != null) {
            summary.append("FoodEx2: ").append(foodex2.getCode()).append("; ");
        }
        
        if (regulatoryCodes != null && !regulatoryCodes.isEmpty()) {
            summary.append("LanguaL Codes: ");
            for (int i = 0; i < Math.min(3, regulatoryCodes.size()); i++) {
                RegulatoryCode code = regulatoryCodes.get(i);
                summary.append(code.getFacetCode()).append(" ");
            }
        }
        
        return summary.toString().trim();
    }
    
    @Override
    public String toString() {
        return "FoodCategoriesEnhanced{" +
                "main='" + main + '\'' +
                ", langualFacets=" + (langualFacets != null ? langualFacets.size() : 0) + " facets" +
                ", foodex2=" + foodex2 +
                ", regulatoryCodes=" + (regulatoryCodes != null ? regulatoryCodes.size() : 0) + " codes" +
                '}';
    }
}
