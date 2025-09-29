package com.example.app.models;

/**
 * Individual classification with embedded regulatory codes.
 * 
 * This represents your brilliant embedded structure where each classification
 * contains all its regulatory information in one place.
 */
public class ClassificationWithCodes {
    
    private String name;          // Human-readable name (e.g., "Animal fats")
    private String facetCodes;    // LanguaL facet codes (e.g., "A0777, A0352")
    private String langualId;     // LanguaL ID (e.g., "A0810")
    
    // Default constructor
    public ClassificationWithCodes() {}
    
    // Constructor
    public ClassificationWithCodes(String name, String facetCodes, String langualId) {
        this.name = name;
        this.facetCodes = facetCodes;
        this.langualId = langualId;
    }
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getFacetCodes() { return facetCodes; }
    public void setFacetCodes(String facetCodes) { this.facetCodes = facetCodes; }
    
    public String getLangualId() { return langualId; }
    public void setLangualId(String langualId) { this.langualId = langualId; }
    
    // Utility methods
    
    /**
     * Get individual facet codes as array.
     */
    public String[] getFacetCodesArray() {
        if (facetCodes != null && !facetCodes.trim().isEmpty()) {
            return facetCodes.split(",\\s*");
        }
        return new String[0];
    }
    
    /**
     * Check if this classification has regulatory codes.
     */
    public boolean hasRegulatoryCodes() {
        return facetCodes != null || langualId != null;
    }
    
    /**
     * Get compliance string for reporting.
     */
    public String getComplianceString() {
        StringBuilder sb = new StringBuilder();
        if (facetCodes != null) {
            sb.append(facetCodes);
        }
        if (langualId != null) {
            if (sb.length() > 0) sb.append("|");
            sb.append(langualId);
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return "ClassificationWithCodes{" +
                "name='" + name + '\'' +
                ", facetCodes='" + facetCodes + '\'' +
                ", langualId='" + langualId + '\'' +
                '}';
    }
}
