#!/usr/bin/env python3
"""
Local Food Image Generator using Stable Diffusion

This script generates child-friendly food images using a local Stable Diffusion model
instead of OpenAI's DALL-E API, making it completely free to run.

Requirements:
1. Install Automatic1111 Stable Diffusion Web UI with API enabled
2. Or use the diffusers library for direct Python integration

Usage: python generate_food_images_local.py
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
from typing import Dict, List, Optional
from io import BytesIO
from PIL import Image
import base64

# Import the helper functions from the original script
from generate_food_images import (
    sanitize_filename, 
    get_unique_foods, 
    save_image, 
    update_database_record,
    MONGODB_URI,
    DATABASE_NAME,
    COLLECTION_NAME,
    IMAGE_OUTPUT_DIR,
    RATE_LIMIT_DELAY
)

# Configuration for local image generation
LOCAL_SD_API_URL = "http://127.0.0.1:7860"  # Default Automatic1111 API URL
USE_DIFFUSERS = False  # Set to True to use diffusers library instead of API

# Set up logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('local_food_image_generation.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

def generate_child_friendly_prompt_local(food_name: str, scientific_name: str) -> tuple:
    """Generate a child-friendly image generation prompt optimized for Stable Diffusion following brand guidelines."""
    try:
        from brand_style_config import build_positive_prompt, build_negative_prompt
        positive_prompt = build_positive_prompt(food_name, scientific_name)
        negative_prompt = build_negative_prompt()
        return positive_prompt, negative_prompt
    except ImportError:
        # Fallback to inline prompts if brand_style_config is not available (with focus emphasis)
        positive_prompt = f"simple flat vector illustration of {food_name}, centered composition, food as main subject, happy smiling character with friendly eyes, clear and in focus, sharp details, minimalist design, line art style, isolated on plain background, single food item only, flat design, simple line-based art, minimal details, soft rounded shapes, clean vector style, playful and inviting, safe and friendly for children, solid color background, 2D illustration, cartoon style, high quality, professional vector art"
        negative_prompt = "realistic, photographic, photo, 3D, complex details, detailed textures, shadows, gradients, realistic lighting, adult themes, scary, dark, text, words, letters, watermark, signature, blurry, low quality, messy, out of focus, unclear, cluttered background, busy design, multiple objects, other food items, plates, bowls, utensils, kitchen items, decorative elements, patterns in background, distracting elements, realistic anatomy, detailed shading, complex patterns"
        return positive_prompt, negative_prompt

def generate_image_with_automatic1111(positive_prompt: str, negative_prompt: str, filename: str) -> Optional[bytes]:
    """Generate an image using Automatic1111 Stable Diffusion Web UI API."""
    
    api_url = f"{LOCAL_SD_API_URL}/sdapi/v1/txt2img"
    
    payload = {
        "prompt": positive_prompt,
        "negative_prompt": negative_prompt,
        "steps": 20,
        "width": 512,
        "height": 512,
        "cfg_scale": 7,
        "sampler_name": "DPM++ 2M Karras",
        "seed": -1,
        "batch_size": 1,
        "n_iter": 1,
    }
    
    try:
        logger.debug(f"Generating image via Automatic1111 API for {filename}")
        response = requests.post(api_url, json=payload, timeout=120)
        
        if response.status_code == 200:
            result = response.json()
            # The image is returned as base64
            image_b64 = result['images'][0]
            image_data = base64.b64decode(image_b64)
            return image_data
        else:
            logger.error(f"Automatic1111 API error: {response.status_code} - {response.text}")
            return None
            
    except requests.exceptions.RequestException as e:
        logger.error(f"Request failed for {filename}: {e}")
        return None

def generate_image_with_diffusers(positive_prompt: str, negative_prompt: str, filename: str) -> Optional[bytes]:
    """Generate an image using the diffusers library directly."""
    
    try:
        # Import diffusers (only when needed)
        from diffusers import StableDiffusionPipeline
        import torch
        
        # Initialize pipeline (you might want to do this once globally)
        if not hasattr(generate_image_with_diffusers, 'pipe'):
            logger.info("Loading Stable Diffusion model... (this may take a while)")
            model_id = "runwayml/stable-diffusion-v1-5"  # or another model
            device = "cuda" if torch.cuda.is_available() else "cpu"
            
            generate_image_with_diffusers.pipe = StableDiffusionPipeline.from_pretrained(
                model_id, 
                torch_dtype=torch.float16 if device == "cuda" else torch.float32
            )
            generate_image_with_diffusers.pipe = generate_image_with_diffusers.pipe.to(device)
            logger.info(f"Model loaded on {device}")
        
        pipe = generate_image_with_diffusers.pipe
        
        # Generate image
        logger.debug(f"Generating image with diffusers for {filename}")
        image = pipe(
            positive_prompt, 
            negative_prompt=negative_prompt,
            num_inference_steps=20,
            height=512,
            width=512,
            guidance_scale=7.5
        ).images[0]
        
        # Convert PIL image to bytes
        img_byte_arr = BytesIO()
        image.save(img_byte_arr, format='JPEG', quality=95)
        return img_byte_arr.getvalue()
        
    except ImportError:
        logger.error("diffusers library not installed. Install with: pip install diffusers transformers")
        return None
    except Exception as e:
        logger.error(f"Diffusers generation failed for {filename}: {e}")
        return None

def test_local_setup():
    """Test if local image generation is working."""
    logger.info("Testing local image generation setup...")
    
    if USE_DIFFUSERS:
        logger.info("Testing diffusers library...")
        test_image = generate_image_with_diffusers(
            "cute cartoon apple with happy face", 
            "realistic, dark, scary", 
            "test.jpg"
        )
    else:
        logger.info(f"Testing Automatic1111 API at {LOCAL_SD_API_URL}...")
        # Test if API is available
        try:
            response = requests.get(f"{LOCAL_SD_API_URL}/sdapi/v1/samplers", timeout=10)
            if response.status_code == 200:
                logger.info("✓ Automatic1111 API is responding")
            else:
                logger.error("✗ Automatic1111 API not responding correctly")
                return False
        except requests.exceptions.RequestException as e:
            logger.error(f"✗ Cannot connect to Automatic1111 API: {e}")
            logger.info("Make sure Automatic1111 is running with --api flag")
            logger.info("Example: ./webui.sh --api")
            return False
        
        test_image = generate_image_with_automatic1111(
            "cute cartoon apple with happy face", 
            "realistic, dark, scary", 
            "test.jpg"
        )
    
    if test_image:
        logger.info("✓ Local image generation is working!")
        return True
    else:
        logger.error("✗ Local image generation failed")
        return False

def main():
    """Main function to generate images for all foods using local models."""
    logger.info("Starting LOCAL food image generation process...")
    
    # Test local setup first
    if not test_local_setup():
        logger.error("Local image generation setup failed. Please check your configuration.")
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
    logger.info(f"Starting image generation for {len(unique_foods)} foods using LOCAL models...")
    logger.info(f"Method: {'Diffusers Library' if USE_DIFFUSERS else 'Automatic1111 API'}")
    
    for food in tqdm(unique_foods, desc="Generating images locally"):
        food_number = food["foodNumber"]
        primary_name = food["primary_name"]
        scientific_name = food["scientific_name"]
        
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
        
        # Generate prompts
        positive_prompt, negative_prompt = generate_child_friendly_prompt_local(primary_name, scientific_name)
        logger.info(f"🎨 Generating local image for: {primary_name} -> {filename}")
        logger.info(f"📝 Positive prompt: {positive_prompt}")
        logger.info(f"🚫 Negative prompt: {negative_prompt}")
        
        # Generate image
        if USE_DIFFUSERS:
            image_data = generate_image_with_diffusers(positive_prompt, negative_prompt, filename)
        else:
            image_data = generate_image_with_automatic1111(positive_prompt, negative_prompt, filename)
        
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
        
        # Small delay to prevent overloading
        time.sleep(0.5)
    
    # Final statistics
    logger.info("=" * 60)
    logger.info("LOCAL FOOD IMAGE GENERATION COMPLETE")
    logger.info("=" * 60)
    logger.info(f"Total foods processed: {len(unique_foods)}")
    logger.info(f"Successfully generated: {success_count}")
    logger.info(f"Already existed (skipped): {skipped_count}")
    logger.info(f"Errors: {error_count}")
    logger.info(f"Images saved to: {IMAGE_OUTPUT_DIR.absolute()}")
    logger.info(f"Method used: {'Diffusers Library' if USE_DIFFUSERS else 'Automatic1111 API'}")
    logger.info("💰 Total cost: $0.00 (FREE!)")
    logger.info("=" * 60)

if __name__ == "__main__":
    main()
