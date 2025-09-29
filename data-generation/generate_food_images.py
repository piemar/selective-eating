#!/usr/bin/env python3
"""
Food Image Generator for Selective Eating App

This script connects to the MongoDB database, fetches all food records,
generates child-friendly images for each food using AI, and updates the
database with the generated image filenames.

Usage: python generate_food_images.py
"""

import os
import sys
import pymongo
import requests
import json
from tqdm import tqdm
from pathlib import Path
import time
import logging
from typing import Dict, List, Optional, Set
from urllib.parse import quote
from io import BytesIO
from PIL import Image

# Configuration - Updated to use EMBEDDED CODES collection (user's brilliant structure)!
MONGODB_URI = "mongodb://localhost:27017"
DATABASE_NAME = "selective_eating_dev"
COLLECTION_NAME = "foods_embedded_codes"           # ← EMBEDDED: User's brilliant embedded regulatory codes!
LEGACY_COLLECTION_NAME = "foods"                   # ← Original collection (kept as backup)
IMAGE_OUTPUT_DIR = Path("image/foods")
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
IMAGE_SIZE = "1024x1024"  # OpenAI DALL-E image size
MAX_RETRIES = 3
RATE_LIMIT_DELAY = 1  # seconds between API calls to avoid rate limits

# Set up logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('food_image_generation.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

def sanitize_filename(filename: str) -> str:
    """Sanitize filename by replacing problematic characters with underscores."""
    # Replace spaces and special characters with underscores
    sanitized = filename.replace(" ", "_")
    # Remove or replace other problematic characters
    chars_to_replace = ['/', '\\', ':', '*', '?', '"', '<', '>', '|', '.', ',', ';']
    for char in chars_to_replace:
        sanitized = sanitized.replace(char, "_")
    # Remove multiple consecutive underscores
    while "__" in sanitized:
        sanitized = sanitized.replace("__", "_")
    # Remove leading/trailing underscores
    sanitized = sanitized.strip("_")
    return sanitized

def get_unique_foods(collection) -> List[Dict]:
    """
    Get unique food records from COMPLETE CLASSIFICATIONS collection (99.1% space savings!).
    
    Uses foods_complete_classifications collection with ALL meaningful classification data.
    Complete food safety, regulatory, and processing information preserved.
    Returns unique foods with both Swedish and English names where available.
    """
    logger.info("Fetching unique foods from COMPLETE CLASSIFICATIONS collection...")
    logger.info(f"🏆 Using {COLLECTION_NAME} - Complete data with 99.1% space savings!")
    
    # Aggregate to group by foodNumber and collect both languages
    pipeline = [
        {
            "$group": {
                "_id": "$foodNumber",
                "foods": {"$push": "$$ROOT"},
                "count": {"$sum": 1}
            }
        },
        {"$sort": {"_id": 1}}
    ]
    
    unique_foods = []
    processed = collection.aggregate(pipeline)
    
    for group in processed:
        food_number = group["_id"]
        foods = group["foods"]
        
        # Find Swedish and English versions
        sv_food = next((f for f in foods if f.get("language") == "sv"), None)
        en_food = next((f for f in foods if f.get("language") == "en"), None)
        
        # Prefer English record to get English names from the name field
        primary_food = en_food or sv_food or foods[0]
        
        if primary_food:
            # Create a combined record using ENGLISH name field only for image generation
            combined_food = {
                "foodNumber": food_number,
                "name_sv": sv_food.get("name", "") if sv_food else "",
                "name_en": en_food.get("name", "") if en_food else "",
                "scientific_name": primary_food.get("scientific_name", ""),
                # Use English name field only for image generation
                "primary_name": en_food.get("name", "") if en_food else "",
                "_id": primary_food.get("_id"),
                "imageUrl": primary_food.get("imageUrl", "")
            }
            unique_foods.append(combined_food)
    
    logger.info(f"Found {len(unique_foods)} unique foods")
    return unique_foods

def generate_child_friendly_prompt(food_name: str, scientific_name: str) -> str:
    """Generate a child-friendly image generation prompt following brand guidelines."""
    try:
        from brand_style_config import build_positive_prompt, build_negative_prompt
        return build_positive_prompt(food_name, scientific_name)
    except ImportError:
        # Fallback to inline prompt if brand_style_config is not available (with focus emphasis)
        prompt = f"Simple flat vector illustration of {food_name}, centered composition, food as main subject, happy smiling character with friendly eyes, clear and in focus, sharp details, simple line art, isolated on plain background, single food item only, flat design, minimal details, playful and inviting, soft rounded shapes, clean vector style, safe and friendly for children, solid color background, no realistic textures, no complex details, no other objects, no plates or utensils, no text or words"
        return prompt

def generate_image_with_openai(prompt: str, filename: str) -> Optional[str]:
    """Generate an image using OpenAI's DALL-E API."""
    if not OPENAI_API_KEY:
        logger.error("OpenAI API key not found. Please set OPENAI_API_KEY environment variable.")
        return None
    
    url = "https://api.openai.com/v1/images/generations"
    headers = {
        "Authorization": f"Bearer {OPENAI_API_KEY}",
        "Content-Type": "application/json"
    }
    
    data = {
        "model": "dall-e-3",
        "prompt": prompt,
        "n": 1,
        "size": IMAGE_SIZE,
        "quality": "standard",
        "style": "vivid"
    }
    
    for attempt in range(MAX_RETRIES):
        try:
            logger.debug(f"Generating image for {filename} (attempt {attempt + 1})")
            response = requests.post(url, headers=headers, json=data, timeout=60)
            
            if response.status_code == 200:
                result = response.json()
                image_url = result["data"][0]["url"]
                
                # Download the image
                img_response = requests.get(image_url, timeout=30)
                if img_response.status_code == 200:
                    return img_response.content
                else:
                    logger.error(f"Failed to download image: {img_response.status_code}")
                    
            elif response.status_code == 429:  # Rate limit
                logger.warning(f"Rate limited, waiting before retry...")
                time.sleep(10)
                continue
            else:
                logger.error(f"OpenAI API error: {response.status_code} - {response.text}")
                
        except requests.exceptions.RequestException as e:
            logger.error(f"Request failed for {filename}: {e}")
            if attempt < MAX_RETRIES - 1:
                time.sleep(2)
                continue
                
    return None

def save_image(image_data: bytes, filepath: Path) -> bool:
    """Save image data to file as JPG."""
    try:
        # Open with PIL and convert to RGB to ensure JPG compatibility
        image = Image.open(BytesIO(image_data))
        if image.mode in ('RGBA', 'P'):
            # Convert to RGB for JPG
            rgb_image = Image.new('RGB', image.size, (255, 255, 255))
            if image.mode == 'P':
                image = image.convert('RGBA')
            rgb_image.paste(image, mask=image.split()[-1] if image.mode == 'RGBA' else None)
            image = rgb_image
        
        # Save as JPG with high quality
        image.save(filepath, 'JPEG', quality=95, optimize=True)
        logger.debug(f"Saved image: {filepath}")
        return True
        
    except Exception as e:
        logger.error(f"Failed to save image {filepath}: {e}")
        return False

def update_database_record(collection, food_id: str, filename: str) -> bool:
    """Update the database record with the image filename."""
    try:
        # Update both Swedish and English versions if they exist
        result = collection.update_many(
            {"foodNumber": food_id},
            {"$set": {"imageUrl": f"image/foods/{filename}"}}
        )
        logger.debug(f"Updated {result.modified_count} records with image filename: {filename}")
        return True
    except Exception as e:
        logger.error(f"Failed to update database for food {food_id}: {e}")
        return False

def main():
    """Main function to generate images for all foods."""
    logger.info("Starting food image generation process...")
    
    # Check if OpenAI API key is available
    if not OPENAI_API_KEY:
        logger.error("OpenAI API key not found. Please set the OPENAI_API_KEY environment variable.")
        logger.info("You can get an API key from: https://platform.openai.com/api-keys")
        return
    
    # Create output directory
    IMAGE_OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    logger.info(f"Created output directory: {IMAGE_OUTPUT_DIR}")
    
    # Connect to MongoDB
    try:
        client = pymongo.MongoClient(MONGODB_URI)
        db = client[DATABASE_NAME]
        collection = db[COLLECTION_NAME]
        
        # Test connection
        collection.find_one()
        logger.info(f"Connected to MongoDB: {MONGODB_URI}/{DATABASE_NAME}")
        
    except Exception as e:
        logger.error(f"Failed to connect to MongoDB: {e}")
        return
    
    # Get unique foods
    unique_foods = get_unique_foods(collection)
    if not unique_foods:
        logger.error("No foods found in database")
        return
    
    # Statistics
    success_count = 0
    error_count = 0
    skipped_count = 0
    
    # Process each food
    logger.info(f"Starting image generation for {len(unique_foods)} foods...")
    
    for food in tqdm(unique_foods, desc="Generating images"):
        food_number = food["foodNumber"]
        name_sv = food["name_sv"]
        name_en = food["name_en"]
        scientific_name = food["scientific_name"]
        primary_name = food["primary_name"]
        
        # Create filename: foodNumber_identifier.jpg (improved logic)
        from improved_filename_generator import create_better_filename
        filename = create_better_filename(food_number, primary_name, scientific_name)
        filepath = IMAGE_OUTPUT_DIR / filename
        
        # Skip if image already exists
        if filepath.exists():
            logger.debug(f"Image already exists for {primary_name}: {filename}")
            skipped_count += 1
            continue
        
        if not primary_name:
            logger.warning(f"No name found for food #{food_number}, skipping...")
            error_count += 1
            continue
        
        # Generate prompt
        prompt = generate_child_friendly_prompt(primary_name, scientific_name)
        logger.info(f"🎨 Generating image for: {primary_name} -> {filename}")
        logger.info(f"📝 Prompt: {prompt}")
        
        # Generate image
        image_data = generate_image_with_openai(prompt, filename)
        
        if image_data:
            # Save image
            if save_image(image_data, filepath):
                # Update database
                if update_database_record(collection, food_number, filename):
                    success_count += 1
                    logger.info(f"✓ Generated and saved: {filename}")
                else:
                    error_count += 1
                    logger.error(f"✗ Failed to update database for: {filename}")
            else:
                error_count += 1
                logger.error(f"✗ Failed to save image: {filename}")
        else:
            error_count += 1
            logger.error(f"✗ Failed to generate image for: {primary_name}")
        
        # Rate limiting
        time.sleep(RATE_LIMIT_DELAY)
    
    # Final statistics
    logger.info("=" * 60)
    logger.info("FOOD IMAGE GENERATION COMPLETE")
    logger.info("=" * 60)
    logger.info(f"Total foods processed: {len(unique_foods)}")
    logger.info(f"Successfully generated: {success_count}")
    logger.info(f"Already existed (skipped): {skipped_count}")
    logger.info(f"Errors: {error_count}")
    logger.info(f"Images saved to: {IMAGE_OUTPUT_DIR.absolute()}")
    logger.info("=" * 60)

if __name__ == "__main__":
    main()
