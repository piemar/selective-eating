package com.example.app.models;

/**
 * Raw material/ingredient used in food production.
 * 
 * This represents the ingredients that make up a food item,
 * including their proportions and preparation methods.
 * 
 * CRITICAL DATA that was missing from optimized collections!
 */
public class RawMaterial {
    
    private String name;            // Name of raw material
    private String foodEx2;         // FoodEx2 code for the raw material
    private String preparation;     // Preparation method (e.g., "Uncooked", "Uncooked/cooked")
    private Double percentage;      // Percentage of this raw material in the final product
    private Double factor;          // Calculation factor
    private Double calculatedAmount; // Calculated amount for raw material
    
    // Default constructor
    public RawMaterial() {}
    
    // Constructor with all fields
    public RawMaterial(String name, String foodEx2, String preparation, 
                      Double percentage, Double factor, Double calculatedAmount) {
        this.name = name;
        this.foodEx2 = foodEx2;
        this.preparation = preparation;
        this.percentage = percentage;
        this.factor = factor;
        this.calculatedAmount = calculatedAmount;
    }
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getFoodEx2() { return foodEx2; }
    public void setFoodEx2(String foodEx2) { this.foodEx2 = foodEx2; }
    
    public String getPreparation() { return preparation; }
    public void setPreparation(String preparation) { this.preparation = preparation; }
    
    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }
    
    public Double getFactor() { return factor; }
    public void setFactor(Double factor) { this.factor = factor; }
    
    public Double getCalculatedAmount() { return calculatedAmount; }
    public void setCalculatedAmount(Double calculatedAmount) { this.calculatedAmount = calculatedAmount; }
    
    // Utility methods
    
    /**
     * Check if this raw material is the primary ingredient (highest percentage).
     */
    public boolean isPrimaryIngredient(Double threshold) {
        return percentage != null && percentage >= threshold;
    }
    
    /**
     * Check if this raw material requires cooking/preparation.
     */
    public boolean requiresCooking() {
        return preparation != null && 
               (preparation.toLowerCase().contains("cooked") || 
                preparation.toLowerCase().contains("cook"));
    }
    
    /**
     * Check if this is an unprocessed raw material.
     */
    public boolean isUncooked() {
        return preparation != null && preparation.toLowerCase().equals("uncooked");
    }
    
    /**
     * Get percentage as formatted string.
     */
    public String getPercentageFormatted() {
        if (percentage == null) return "N/A";
        return String.format("%.1f%%", percentage);
    }
    
    /**
     * Get display name for ingredient lists.
     */
    public String getDisplayName() {
        if (name != null && !name.trim().isEmpty()) {
            return name;
        }
        return foodEx2 != null ? foodEx2 : "Unknown ingredient";
    }
    
    /**
     * Check if this raw material has complete data.
     */
    public boolean isComplete() {
        return name != null && !name.trim().isEmpty() &&
               foodEx2 != null && !foodEx2.trim().isEmpty() &&
               percentage != null && percentage >= 0;
    }
    
    @Override
    public String toString() {
        return "RawMaterial{" +
                "name='" + name + '\'' +
                ", foodEx2='" + foodEx2 + '\'' +
                ", preparation='" + preparation + '\'' +
                ", percentage=" + percentage +
                ", factor=" + factor +
                ", calculatedAmount=" + calculatedAmount +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        RawMaterial that = (RawMaterial) o;
        
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (foodEx2 != null ? !foodEx2.equals(that.foodEx2) : that.foodEx2 != null) return false;
        return percentage != null ? percentage.equals(that.percentage) : that.percentage == null;
    }
    
    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (foodEx2 != null ? foodEx2.hashCode() : 0);
        result = 31 * result + (percentage != null ? percentage.hashCode() : 0);
        return result;
    }
}
