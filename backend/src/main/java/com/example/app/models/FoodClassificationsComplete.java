package com.example.app.models;

import java.util.List;
import java.util.Map;

/**
 * Complete food classifications that match the foods_complete_classifications collection structure.
 * 
 * This model provides ALL meaningful classification data with clean field names
 * and complete compliance codes for regulatory purposes.
 */
public class FoodClassificationsComplete {
    
    private String productType;      // e.g., "Animal fats"
    private String foodSource;       // e.g., "Cattle" 
    private String partUsed;         // e.g., "Fat Or Oil"
    private String physicalState;    // e.g., "Semisolid"
    private String heatTreatment;    // e.g., "No heat treatment"
    private String preservation;     // e.g., "Chilled"
    private String packingMedium;    // e.g., "No packing medium"
    private String consumerGroup;    // e.g., "Human consumption"
    private String geographicOrigin; // e.g., "Sweden"
    private String cookingMethod;    // e.g., "No cooking" (optional)
    private String treatmentApplied; // e.g., "Not applied" (optional)
    private String contactSurface;   // e.g., "Unknown" (optional)
    private String containerWrapping; // e.g., "Unknown" (optional)
    
    // FoodEx2 classification  
    private String foodex2;         // e.g., "A037X"
    
    // Structured regulatory codes for easy access (ONLY structure needed)
    private RegulatoryCodesStructure regulatoryCodes;
    
    // Default constructor
    public FoodClassificationsComplete() {}
    
    // Constructor with essential fields
    public FoodClassificationsComplete(String productType, String foodSource, String partUsed) {
        this.productType = productType;
        this.foodSource = foodSource;
        this.partUsed = partUsed;
    }
    
    // Getters and Setters
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    
    public String getFoodSource() { return foodSource; }
    public void setFoodSource(String foodSource) { this.foodSource = foodSource; }
    
    public String getPartUsed() { return partUsed; }
    public void setPartUsed(String partUsed) { this.partUsed = partUsed; }
    
    public String getPhysicalState() { return physicalState; }
    public void setPhysicalState(String physicalState) { this.physicalState = physicalState; }
    
    public String getHeatTreatment() { return heatTreatment; }
    public void setHeatTreatment(String heatTreatment) { this.heatTreatment = heatTreatment; }
    
    public String getPreservation() { return preservation; }
    public void setPreservation(String preservation) { this.preservation = preservation; }
    
    public String getPackingMedium() { return packingMedium; }
    public void setPackingMedium(String packingMedium) { this.packingMedium = packingMedium; }
    
    public String getConsumerGroup() { return consumerGroup; }
    public void setConsumerGroup(String consumerGroup) { this.consumerGroup = consumerGroup; }
    
    public String getGeographicOrigin() { return geographicOrigin; }
    public void setGeographicOrigin(String geographicOrigin) { this.geographicOrigin = geographicOrigin; }
    
    public String getCookingMethod() { return cookingMethod; }
    public void setCookingMethod(String cookingMethod) { this.cookingMethod = cookingMethod; }
    
    public String getTreatmentApplied() { return treatmentApplied; }
    public void setTreatmentApplied(String treatmentApplied) { this.treatmentApplied = treatmentApplied; }
    
    public String getContactSurface() { return contactSurface; }
    public void setContactSurface(String contactSurface) { this.contactSurface = contactSurface; }
    
    public String getContainerWrapping() { return containerWrapping; }
    public void setContainerWrapping(String containerWrapping) { this.containerWrapping = containerWrapping; }
    
    public String getFoodex2() { return foodex2; }
    public void setFoodex2(String foodex2) { this.foodex2 = foodex2; }
    
    public RegulatoryCodesStructure getRegulatoryCode() { return regulatoryCodes; }
    public void setRegulatoryCode(RegulatoryCodesStructure regulatoryCodes) { this.regulatoryCodes = regulatoryCodes; }
    
    // Utility methods
    
    /**
     * Check if this food requires heat treatment.
     */
    public boolean requiresHeatTreatment() {
        return heatTreatment != null && 
               !heatTreatment.toLowerCase().contains("no heat") &&
               !heatTreatment.toLowerCase().contains("not heat");
    }
    
    /**
     * Check if this food requires refrigeration.
     */
    public boolean requiresRefrigeration() {
        return preservation != null && 
               (preservation.toLowerCase().contains("chill") ||
                preservation.toLowerCase().contains("refrigerat") ||
                preservation.toLowerCase().contains("cold"));
    }
    
    /**
     * Check if this is an animal-derived product.
     */
    public boolean isAnimalProduct() {
        if (foodSource != null) {
            String source = foodSource.toLowerCase();
            return source.contains("cattle") || source.contains("swine") ||
                   source.contains("poultry") || source.contains("fish") ||
                   source.contains("animal");
        }
        if (productType != null) {
            String type = productType.toLowerCase();
            return type.contains("animal") || type.contains("meat") ||
                   type.contains("dairy");
        }
        return false;
    }
    
    /**
     * Check if this is a plant-based product.
     */
    public boolean isPlantBased() {
        if (foodSource != null) {
            String source = foodSource.toLowerCase();
            return source.contains("plant") || source.contains("fruit") ||
                   source.contains("vegetable") || source.contains("grain") ||
                   source.contains("cereal");
        }
        return false;
    }
    
    /**
     * Get LanguaL ID for a specific classification type from structured regulatory codes.
     */
    public String getLanguaLId(String classificationType) {
        if (regulatoryCodes != null && regulatoryCodes.getLangual() != null) {
            for (LanguaLCode code : regulatoryCodes.getLangual()) {
                if (classificationType.equals(code.getType())) {
                    return code.getLangualId();
                }
            }
        }
        return null;
    }
    
    /**
     * Get facet codes for a specific classification type from structured regulatory codes.
     */
    public String getFacetCodes(String classificationType) {
        if (regulatoryCodes != null && regulatoryCodes.getLangual() != null) {
            for (LanguaLCode code : regulatoryCodes.getLangual()) {
                if (classificationType.equals(code.getType())) {
                    return code.getFacetCodes();
                }
            }
        }
        return null;
    }
    
    /**
     * Get FoodEx2 code from regulatory codes.
     */
    public String getFoodEx2Code() {
        if (regulatoryCodes != null) {
            return regulatoryCodes.getFoodex2();
        }
        return foodex2; // Fallback to direct field
    }
    
    /**
     * Get display name for UI purposes.
     */
    public String getDisplayName() {
        if (productType != null && !productType.isEmpty()) {
            return productType;
        }
        if (foodSource != null && !foodSource.isEmpty()) {
            return foodSource;
        }
        return "Unknown classification";
    }
    
    /**
     * Get all regulatory codes summary for compliance reporting.
     */
    public String getComplianceSummary() {
        StringBuilder summary = new StringBuilder();
        
        // Add FoodEx2 if available
        if (regulatoryCodes != null && regulatoryCodes.getFoodex2() != null) {
            summary.append("FoodEx2: ").append(regulatoryCodes.getFoodex2()).append("; ");
        }
        
        // Add LanguaL codes if available
        if (regulatoryCodes != null && regulatoryCodes.getLangual() != null && !regulatoryCodes.getLangual().isEmpty()) {
            summary.append("LanguaL: ");
            for (LanguaLCode code : regulatoryCodes.getLangual()) {
                summary.append(code.getType())
                       .append("(").append(code.getFacetCodes())
                       .append("|").append(code.getLangualId())
                       .append(") ");
            }
        }
        
        return summary.toString().trim();
    }
    
    @Override
    public String toString() {
        return "FoodClassificationsComplete{" +
                "productType='" + productType + '\'' +
                ", foodSource='" + foodSource + '\'' +
                ", partUsed='" + partUsed + '\'' +
                ", physicalState='" + physicalState + '\'' +
                ", heatTreatment='" + heatTreatment + '\'' +
                ", preservation='" + preservation + '\'' +
                ", geographicOrigin='" + geographicOrigin + '\'' +
                ", foodex2='" + foodex2 + '\'' +
                ", regulatoryCodes=" + regulatoryCodes +
                '}';
    }
    
    // Nested class for structured regulatory codes
    public static class RegulatoryCodesStructure {
        private List<LanguaLCode> langual;
        private String foodex2;
        
        // Default constructor
        public RegulatoryCodesStructure() {}
        
        // Getters and setters
        public List<LanguaLCode> getLangual() { return langual; }
        public void setLangual(List<LanguaLCode> langual) { this.langual = langual; }
        
        public String getFoodex2() { return foodex2; }
        public void setFoodex2(String foodex2) { this.foodex2 = foodex2; }
        
        @Override
        public String toString() {
            return "RegulatoryCodesStructure{" +
                    "langual=" + (langual != null ? langual.size() : 0) + " codes" +
                    ", foodex2='" + foodex2 + '\'' +
                    '}';
        }
    }
    
    // Nested class for LanguaL codes
    public static class LanguaLCode {
        private String type;        // e.g., "product_type"
        private String facetCodes;  // e.g., "A0777, A0352"  
        private String langualId;   // e.g., "A0810"
        
        // Default constructor
        public LanguaLCode() {}
        
        // Constructor
        public LanguaLCode(String type, String facetCodes, String langualId) {
            this.type = type;
            this.facetCodes = facetCodes;
            this.langualId = langualId;
        }
        
        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getFacetCodes() { return facetCodes; }
        public void setFacetCodes(String facetCodes) { this.facetCodes = facetCodes; }
        
        public String getLangualId() { return langualId; }
        public void setLangualId(String langualId) { this.langualId = langualId; }
        
        @Override
        public String toString() {
            return "LanguaLCode{" +
                    "type='" + type + '\'' +
                    ", facetCodes='" + facetCodes + '\'' +
                    ", langualId='" + langualId + '\'' +
                    '}';
        }
    }
}
