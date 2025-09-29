#!/usr/bin/env python3
"""
Brand Style Test Script

This script shows you exactly what prompts will be generated for different foods
using your brand style configuration. Run this to preview the style before
generating actual images.

Usage: python test_brand_style.py
"""

from brand_style_config import (
    build_positive_prompt, 
    build_negative_prompt, 
    get_food_category,
    get_brand_style_summary,
    BRAND_NAME
)

def test_brand_style():
    """Test the brand style configuration with sample foods."""
    
    print("🎨 BRAND STYLE CONFIGURATION TEST")
    print("=" * 60)
    
    # Show brand summary
    summary = get_brand_style_summary()
    print(f"Brand: {summary['brand']}")
    print(f"Style: {summary['style']}")
    print(f"Approach: {summary['approach']}")
    print(f"Target: {summary['target']}")
    print(f"Avoid: {summary['avoid']}")
    print()
    
    # Test foods from your database (based on the sample we saw)
    test_foods = [
        ("Beef tallow", "Bos taurus"),
        ("Apple", "Malus domestica"),  
        ("Carrot", "Daucus carota"),
        ("Bread", "Triticum aestivum"),
        ("Milk", "Bos taurus"),
        ("Chicken", "Gallus gallus"),
        ("Broccoli", "Brassica oleracea"),
        ("Rice", "Oryza sativa"),
        ("Cheese", ""),
        ("Banana", "Musa acuminata")
    ]
    
    print("🍽️ SAMPLE FOOD PROMPTS")
    print("=" * 60)
    
    for food_name, scientific_name in test_foods:
        category = get_food_category(food_name)
        positive_prompt = build_positive_prompt(food_name, scientific_name)
        negative_prompt = build_negative_prompt()
        
        print(f"Food: {food_name}")
        if scientific_name:
            print(f"Scientific: {scientific_name}")
        print(f"Category: {category}")
        print(f"Positive Prompt:")
        print(f"  {positive_prompt}")
        print(f"Negative Prompt:")
        print(f"  {negative_prompt}")
        print("-" * 40)
    
    print()
    print("✅ KEY STYLE ELEMENTS INCLUDED:")
    print("• Simple flat vector illustration")
    print("• Minimalist design with line art style") 
    print("• Happy, smiling characters with friendly eyes")
    print("• Soft rounded shapes and flat colors")
    print("• Clean vector style, safe for children")
    print("• Solid color backgrounds")
    print()
    
    print("❌ ELEMENTS BEING AVOIDED:")
    print("• Realistic photos or 3D renders")
    print("• Complex details or textures")
    print("• Dark, scary, or adult themes")
    print("• Text, watermarks, or signatures")
    print("• Cluttered or messy backgrounds")
    print()
    
    print("🎯 This style will create:")
    print("• Child-friendly, playful food characters")
    print("• Consistent flat design across all foods")
    print("• Safe, inviting visual appearance")  
    print("• Perfect for educational food apps")
    print()
    
    print("🚀 NEXT STEPS:")
    print("1. Review the prompts above")
    print("2. Adjust brand_style_config.py if needed")
    print("3. Test with sample generation:")
    print("   pipenv run python generate_sample_images_local.py")
    print("4. Generate all images:")
    print("   pipenv run python generate_food_images_local.py")

def preview_category_styles():
    """Show how different food categories will be styled."""
    
    print("\n🎨 CATEGORY-SPECIFIC STYLING")
    print("=" * 60)
    
    categories = {
        "Fruits": "Apple",
        "Vegetables": "Carrot", 
        "Proteins": "Chicken",
        "Grains": "Bread",
        "Dairy": "Milk"
    }
    
    for category_name, example_food in categories.items():
        category = get_food_category(example_food)
        print(f"{category_name} (example: {example_food})")
        print(f"Category code: {category}")
        
        # Show just the food-specific part of the prompt
        from brand_style_config import FOOD_STYLE_CUSTOMIZATIONS
        style_config = FOOD_STYLE_CUSTOMIZATIONS[category]
        print(f"Features: {style_config['features']}")
        print(f"Style: {style_config['style']}")
        print("-" * 30)

if __name__ == "__main__":
    test_brand_style()
    preview_category_styles()
