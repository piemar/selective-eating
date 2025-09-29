package com.example.app.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Embedded food classifications structure.
 * 
 * Each classification field contains both the human-readable name 
 * and its regulatory codes in one place - your brilliant design!
 */
public class FoodClassificationsEmbedded {
    
    // Each classification with embedded codes
    private ClassificationWithCodes productType;
    private ClassificationWithCodes foodSource;
    private ClassificationWithCodes partUsed;
    private ClassificationWithCodes physicalState;
    private ClassificationWithCodes heatTreatment;
    private ClassificationWithCodes preservation;
    private ClassificationWithCodes packingMedium;
    private ClassificationWithCodes consumerGroup;
    private ClassificationWithCodes geographicOrigin;
    private ClassificationWithCodes contactSurface;
    private ClassificationWithCodes containerWrapping;
    private ClassificationWithCodes cookingMethod;
    private ClassificationWithCodes treatmentApplied;
    
    // FoodEx2 remains a simple string
    private String foodex2;
    
    // Default constructor
    public FoodClassificationsEmbedded() {}
    
    // Getters and Setters
    public ClassificationWithCodes getProductType() { return productType; }
    public void setProductType(ClassificationWithCodes productType) { this.productType = productType; }
    
    public ClassificationWithCodes getFoodSource() { return foodSource; }
    public void setFoodSource(ClassificationWithCodes foodSource) { this.foodSource = foodSource; }
    
    public ClassificationWithCodes getPartUsed() { return partUsed; }
    public void setPartUsed(ClassificationWithCodes partUsed) { this.partUsed = partUsed; }
    
    public ClassificationWithCodes getPhysicalState() { return physicalState; }
    public void setPhysicalState(ClassificationWithCodes physicalState) { this.physicalState = physicalState; }
    
    public ClassificationWithCodes getHeatTreatment() { return heatTreatment; }
    public void setHeatTreatment(ClassificationWithCodes heatTreatment) { this.heatTreatment = heatTreatment; }
    
    public ClassificationWithCodes getPreservation() { return preservation; }
    public void setPreservation(ClassificationWithCodes preservation) { this.preservation = preservation; }
    
    public ClassificationWithCodes getPackingMedium() { return packingMedium; }
    public void setPackingMedium(ClassificationWithCodes packingMedium) { this.packingMedium = packingMedium; }
    
    public ClassificationWithCodes getConsumerGroup() { return consumerGroup; }
    public void setConsumerGroup(ClassificationWithCodes consumerGroup) { this.consumerGroup = consumerGroup; }
    
    public ClassificationWithCodes getGeographicOrigin() { return geographicOrigin; }
    public void setGeographicOrigin(ClassificationWithCodes geographicOrigin) { this.geographicOrigin = geographicOrigin; }
    
    public ClassificationWithCodes getContactSurface() { return contactSurface; }
    public void setContactSurface(ClassificationWithCodes contactSurface) { this.contactSurface = contactSurface; }
    
    public ClassificationWithCodes getContainerWrapping() { return containerWrapping; }
    public void setContainerWrapping(ClassificationWithCodes containerWrapping) { this.containerWrapping = containerWrapping; }
    
    public ClassificationWithCodes getCookingMethod() { return cookingMethod; }
    public void setCookingMethod(ClassificationWithCodes cookingMethod) { this.cookingMethod = cookingMethod; }
    
    public ClassificationWithCodes getTreatmentApplied() { return treatmentApplied; }
    public void setTreatmentApplied(ClassificationWithCodes treatmentApplied) { this.treatmentApplied = treatmentApplied; }
    
    public String getFoodex2() { return foodex2; }
    public void setFoodex2(String foodex2) { this.foodex2 = foodex2; }
    
    // Utility methods
    
    /**
     * Get any classification by name (dynamic access).
     */
    public ClassificationWithCodes getClassification(String type) {
        switch (type.toLowerCase().replace("_", "")) {
            case "producttype": return productType;
            case "foodsource": return foodSource;
            case "partused": return partUsed;
            case "physicalstate": return physicalState;
            case "heattreatment": return heatTreatment;
            case "preservation": return preservation;
            case "packingmedium": return packingMedium;
            case "consumergroup": return consumerGroup;
            case "geographicorigin": return geographicOrigin;
            case "contactsurface": return contactSurface;
            case "containerwrapping": return containerWrapping;
            case "cookingmethod": return cookingMethod;
            case "treatmentapplied": return treatmentApplied;
            default: return null;
        }
    }
    
    /**
     * Get all classifications as a map for reporting.
     */
    public Map<String, ClassificationWithCodes> getAllClassifications() {
        Map<String, ClassificationWithCodes> map = new HashMap<>();
        if (productType != null) map.put("product_type", productType);
        if (foodSource != null) map.put("food_source", foodSource);
        if (partUsed != null) map.put("part_used", partUsed);
        if (physicalState != null) map.put("physical_state", physicalState);
        if (heatTreatment != null) map.put("heat_treatment", heatTreatment);
        if (preservation != null) map.put("preservation", preservation);
        if (packingMedium != null) map.put("packing_medium", packingMedium);
        if (consumerGroup != null) map.put("consumer_group", consumerGroup);
        if (geographicOrigin != null) map.put("geographic_origin", geographicOrigin);
        if (contactSurface != null) map.put("contact_surface", contactSurface);
        if (containerWrapping != null) map.put("container_wrapping", containerWrapping);
        if (cookingMethod != null) map.put("cooking_method", cookingMethod);
        if (treatmentApplied != null) map.put("treatment_applied", treatmentApplied);
        return map;
    }
    
    /**
     * Generate compliance summary for all classifications.
     */
    public String getComplianceSummary() {
        StringBuilder summary = new StringBuilder();
        
        // Add FoodEx2 if available
        if (foodex2 != null) {
            summary.append("FoodEx2: ").append(foodex2).append("; ");
        }
        
        // Add all classifications with codes
        Map<String, ClassificationWithCodes> all = getAllClassifications();
        for (Map.Entry<String, ClassificationWithCodes> entry : all.entrySet()) {
            ClassificationWithCodes classification = entry.getValue();
            if (classification != null && classification.hasRegulatoryCodes()) {
                summary.append(entry.getKey())
                       .append("(").append(classification.getComplianceString()).append(") ");
            }
        }
        
        return summary.toString().trim();
    }
    
    @Override
    public String toString() {
        return "FoodClassificationsEmbedded{" +
                "productType=" + (productType != null ? productType.getName() : "null") +
                ", foodSource=" + (foodSource != null ? foodSource.getName() : "null") +
                ", foodex2='" + foodex2 + '\'' +
                '}';
    }
}
