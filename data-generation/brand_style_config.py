"""
Brand Style Configuration for Food Image Generation

This file defines the visual style guidelines for generating child-friendly food images
that match your brand identity.

You can easily customize the style by modifying the values below.
"""

# ===== BRAND STYLE SETTINGS =====

BRAND_NAME = "bala"  # Your brand name (can be included in prompts if needed)

# Core visual style
VISUAL_STYLE = {
    "base_style": "simple flat vector illustration",
    "design_approach": "minimalist design, line art style, food-focused composition",
    "color_palette": "flat colors, soft rounded shapes",
    "background": "plain solid color background, no other objects",
    "complexity": "minimal details, clean vector style, single food item only"
}

# Character design guidelines
CHARACTER_STYLE = {
    "expressions": "happy, smiling, cheerful, friendly",
    "eyes": "big friendly eyes",
    "shapes": "soft rounded shapes, simple geometric shapes",
    "safety": "playful and inviting, safe and friendly for children"
}

# Art style specifications
ART_SPECIFICATIONS = {
    "medium": "2D illustration, cartoon style, flat design",
    "technique": "simple line-based art, clean line design",
    "quality": "high quality, professional vector art",
    "format": "vector illustration style"
}

# Food-specific customizations
FOOD_STYLE_CUSTOMIZATIONS = {
    "fruits": {
        "features": "happy smiling fruit with big friendly eyes",
        "style": "simple line art, flat colors, vibrant but soft colors"
    },
    "vegetables": {
        "features": "smiling vegetable character with cheerful expression", 
        "style": "clean line design, flat vector, natural but simplified colors"
    },
    "proteins": {
        "features": "abstract cute food shape with happy face",
        "style": "simplified geometric form, not realistic, playful interpretation"
    },
    "grains": {
        "features": "friendly cartoon grain/bread with warm smile",
        "style": "simple geometric shapes, flat design, warm golden tones"
    },
    "dairy": {
        "features": "cute dairy character with soft rounded features",
        "style": "minimal flat design, soft pastel colors"
    },
    "default": {
        "features": "adorable food mascot with playful expression",
        "style": "abstract geometric shapes, friendly and approachable"
    }
}

# Elements to avoid (negative prompts)
AVOID_ELEMENTS = [
    # Realism
    "realistic", "photographic", "photo", "3D", "realistic anatomy",
    
    # Complex details
    "complex details", "detailed textures", "detailed shading",
    "realistic lighting", "shadows", "gradients", "complex patterns",
    
    # Background distractions
    "cluttered background", "busy design", "multiple objects", "other food items",
    "plates", "bowls", "utensils", "kitchen items", "decorative elements",
    "patterns in background", "busy background", "distracting elements",
    
    # Unwanted content
    "scary", "dark", "adult themes", "text", "words", "letters",
    "watermark", "signature",
    
    # Quality issues
    "blurry", "low quality", "messy", "out of focus", "unclear"
]

# ===== HELPER FUNCTIONS =====

def get_food_category(food_name: str) -> str:
    """Determine the food category based on the name."""
    food_lower = food_name.lower()
    
    if any(word in food_lower for word in ['fruit', 'apple', 'banana', 'orange', 'berry', 'grape', 'peach', 'pear']):
        return 'fruits'
    elif any(word in food_lower for word in ['vegetable', 'carrot', 'broccoli', 'tomato', 'lettuce', 'spinach', 'pepper']):
        return 'vegetables'
    elif any(word in food_lower for word in ['meat', 'beef', 'chicken', 'fish', 'pork', 'protein', 'egg']):
        return 'proteins'
    elif any(word in food_lower for word in ['bread', 'pasta', 'rice', 'grain', 'wheat', 'cereal', 'oats']):
        return 'grains'
    elif any(word in food_lower for word in ['dairy', 'milk', 'cheese', 'yogurt', 'butter', 'cream']):
        return 'dairy'
    else:
        return 'default'

def build_positive_prompt(food_name: str, scientific_name: str = "") -> str:
    """Build a complete positive prompt following brand guidelines with focus emphasis."""
    category = get_food_category(food_name)
    style_config = FOOD_STYLE_CUSTOMIZATIONS[category]
    
    # Base prompt with focus emphasis
    prompt_parts = [
        f"{VISUAL_STYLE['base_style']} of {food_name}",
        "centered composition, food as main subject",
        VISUAL_STYLE['design_approach'], 
        style_config['features'],
        style_config['style'],
        "clear and in focus, sharp details",
        VISUAL_STYLE['color_palette'],
        CHARACTER_STYLE['safety'],
        VISUAL_STYLE['background'],
        "isolated on plain background",
        ART_SPECIFICATIONS['medium'],
        ART_SPECIFICATIONS['technique'],
        VISUAL_STYLE['complexity'],
        ART_SPECIFICATIONS['quality']
    ]
    
    return ", ".join(prompt_parts)

def build_negative_prompt() -> str:
    """Build a comprehensive negative prompt."""
    return ", ".join(AVOID_ELEMENTS)

def get_brand_style_summary() -> dict:
    """Get a summary of the current brand style settings."""
    return {
        "brand": BRAND_NAME,
        "style": "Simple, flat, line-based with playful touches",
        "approach": "Abstract food shapes and cute illustrations",
        "target": "Safe and inviting for children",
        "avoid": "Realistic photos, complex details, scary elements"
    }

# Example usage:
if __name__ == "__main__":
    # Test the configuration
    test_foods = ["apple", "carrot", "chicken", "bread", "milk"]
    
    print("=== Brand Style Configuration Test ===")
    print(f"Brand: {BRAND_NAME}")
    print(f"Style Summary: {get_brand_style_summary()}")
    print()
    
    for food in test_foods:
        print(f"Food: {food}")
        print(f"Category: {get_food_category(food)}")
        print(f"Positive prompt: {build_positive_prompt(food)}")
        print(f"Negative prompt: {build_negative_prompt()}")
        print("-" * 50)
