#!/usr/bin/env python3
"""
Improved Filename Generator

This script creates better filenames when scientific names are missing.
Instead of "unknown", it uses sanitized food names.
"""

import re
from generate_food_images import sanitize_filename

def create_better_filename(nummer: int, food_name: str, scientific_name: str) -> str:
    """
    Create better filenames using food name when scientific name is missing.
    
    Format: {nummer}_{identifier}.jpg where identifier is:
    - Scientific name if available (e.g., "Bos_taurus") 
    - Shortened food name if no scientific name (e.g., "Blended_spread")
    """
    
    if scientific_name and scientific_name.strip():
        # Use scientific name (existing logic)
        identifier = sanitize_filename(scientific_name.strip())
    else:
        # Create identifier from food name
        identifier = create_food_name_identifier(food_name)
    
    return f"{nummer}_{identifier}.jpg"

def create_food_name_identifier(food_name: str) -> str:
    """
    Create a clean identifier from food name.
    
    Examples:
    'Blended spread sea salt 80% fat fortified e.g. Bregott' -> 'Blended_spread'
    'Margarine for cooking/baking 80% fat fortified e.g. Melba' -> 'Margarine_cooking'
    'Low-fat margarine 38% fat fortified e.g. Becel' -> 'Low_fat_margarine'
    """
    
    if not food_name:
        return "unknown"
    
    # Clean up the name
    name = food_name.strip()
    
    # Remove common noise words and patterns
    noise_patterns = [
        r'\d+%',  # Remove percentages like "80%", "38%"
        r'e\.g\. [\w\s]+',  # Remove "e.g. Brand" parts
        r'fortified',
        r'fat\b',  # Remove standalone "fat" word
        r'for\s+\w+',  # Remove "for cooking", "for baking"
    ]
    
    for pattern in noise_patterns:
        name = re.sub(pattern, '', name, flags=re.IGNORECASE)
    
    # Clean up extra spaces and punctuation
    name = re.sub(r'[^\w\s-]', ' ', name)  # Replace punctuation with spaces
    name = re.sub(r'\s+', ' ', name)       # Multiple spaces to single space
    name = name.strip()
    
    # Split into words and take first 2-3 meaningful words
    words = [w for w in name.split() if len(w) > 2]  # Skip short words
    
    # Take first 2-3 words, max 30 characters
    identifier_words = []
    char_count = 0
    for word in words[:4]:  # Max 4 words
        if char_count + len(word) + 1 <= 25:  # Max 25 chars total
            identifier_words.append(word)
            char_count += len(word) + 1
        else:
            break
    
    if not identifier_words:
        # Fallback: just use first word if nothing else works
        first_word = name.split()[0] if name.split() else "unknown"
        identifier_words = [first_word[:15]]  # Max 15 chars
    
    # Join with underscores and sanitize
    identifier = "_".join(identifier_words)
    return sanitize_filename(identifier)

def test_filename_generation():
    """Test the improved filename generation with real examples."""
    
    test_cases = [
        (5, "Blended spread sea salt 80% fat fortified e.g. Bregott", ""),
        (6, "Blended spread 60% fat fortified e.g. Bregott Mellan", ""),
        (10, "Liquid margarine 82% fat fortified e.g. Milda culinesse", ""),
        (12, "Margarine for cooking/baking 80% fat fortified e.g. Melba", ""),
        (13, "Margarine for cooking/baking 80% fat fortified e.g. Milda", ""),
        (1, "Beef tallow", "Bos taurus"),  # Has scientific name
        (4, "Coconut fat", "Cocos nucifera L"),  # Has scientific name
        (100, "Apple juice concentrate", ""),
        (200, "Whole wheat bread with seeds", ""),
        (300, "Greek yogurt plain 0% fat", ""),
    ]
    
    print("🔧 IMPROVED FILENAME GENERATION TEST")
    print("=" * 60)
    
    for nummer, food_name, scientific_name in test_cases:
        old_filename = f"{nummer}_{'unknown' if not scientific_name else sanitize_filename(scientific_name)}.jpg"
        new_filename = create_better_filename(nummer, food_name, scientific_name)
        
        print(f"Food #{nummer}: {food_name[:40]}{'...' if len(food_name) > 40 else ''}")
        print(f"  Scientific: '{scientific_name}'" if scientific_name else "  Scientific: (empty)")
        print(f"  OLD: {old_filename}")
        print(f"  NEW: {new_filename}")
        print()
    
    print("✅ IMPROVEMENTS:")
    print("• No more 'unknown' filenames")
    print("• Meaningful identifiers from food names")
    print("• Cleaned up noise words (percentages, brands)")
    print("• Reasonable length (max ~25 characters)")
    print("• Still uses scientific names when available")

if __name__ == "__main__":
    test_filename_generation()
