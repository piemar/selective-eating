package com.example.app.models;

/**
 * Improved food categories structure that preserves useful classification information
 * while keeping documents small and efficient.
 * 
 * This replaces the verbose 16+ classification objects from the original documents
 * with a clean, structured approach that keeps the most useful information.
 */
public class FoodCategories {
    
    private String main;     // Main group (e.g., "Other fats (lard, tallow, coconut oil)")
    private String type;     // Product type (e.g., "Animal fats")  
    private String source;   // Food source (e.g., "Cattle", "Swine")
    private String part;     // Part of plant/animal (e.g., "Fat/Oil")
    private String foodex2;  // FoodEx2 code for EU compliance (e.g., "A037X")
    
    // Default constructor
    public FoodCategories() {
    }
    
    // Constructor with main category
    public FoodCategories(String main) {
        this.main = main;
    }
    
    // Getters and Setters
    public String getMain() {
        return main;
    }
    
    public void setMain(String main) {
        this.main = main;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public String getPart() {
        return part;
    }
    
    public void setPart(String part) {
        this.part = part;
    }
    
    public String getFoodex2() {
        return foodex2;
    }
    
    public void setFoodex2(String foodex2) {
        this.foodex2 = foodex2;
    }
    
    /**
     * Get a simple display name for UI purposes.
     * Prioritizes main category, falls back to type or source.
     */
    public String getDisplayName() {
        if (main != null && !main.isEmpty()) {
            return main;
        }
        if (type != null && !type.isEmpty()) {
            return type;
        }
        if (source != null && !source.isEmpty()) {
            return source;
        }
        return "Unknown category";
    }
    
    /**
     * Check if this food is in a specific category.
     * Useful for filtering and searching.
     */
    public boolean isInCategory(String category) {
        if (category == null || category.isEmpty()) {
            return false;
        }
        
        String lowerCategory = category.toLowerCase();
        
        return (main != null && main.toLowerCase().contains(lowerCategory)) ||
               (type != null && type.toLowerCase().contains(lowerCategory)) ||
               (source != null && source.toLowerCase().contains(lowerCategory)) ||
               (part != null && part.toLowerCase().contains(lowerCategory));
    }
    
    /**
     * Check if this is a specific type of food (for dietary restrictions, etc.)
     */
    public boolean isAnimalProduct() {
        return isInCategory("animal") || isInCategory("cattle") || 
               isInCategory("swine") || isInCategory("meat");
    }
    
    public boolean isPlantBased() {
        return isInCategory("plant") || isInCategory("fruit") || 
               isInCategory("vegetable") || isInCategory("grain");
    }
    
    public boolean isDairyProduct() {
        return isInCategory("dairy") || isInCategory("milk") || 
               isInCategory("cheese");
    }
    
    @Override
    public String toString() {
        return "FoodCategories{" +
                "main='" + main + '\'' +
                ", type='" + type + '\'' +
                ", source='" + source + '\'' +
                ", part='" + part + '\'' +
                ", foodex2='" + foodex2 + '\'' +
                '}';
    }
}
