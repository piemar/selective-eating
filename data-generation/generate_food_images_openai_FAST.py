#!/usr/bin/env python3
"""
SPEED-OPTIMIZED OpenAI Food Image Generator

This version uses parallel requests to OpenAI's API for maximum speed.
Expected speedup: 5-10x faster than sequential requests.

Usage: python generate_food_images_openai_FAST.py
"""

import os
import sys
import pymongo
import asyncio
import aiohttp
import json
from tqdm import tqdm
from pathlib import Path
import time
import logging
from typing import Dict, List, Optional
from io import BytesIO
from PIL import Image

# Import the helper functions from the original script
from generate_food_images import (
    sanitize_filename, 
    get_unique_foods, 
    save_image, 
    update_database_record,
    MONGODB_URI,
    DATABASE_NAME,
    COLLECTION_NAME,
    IMAGE_OUTPUT_DIR
)

# Configuration for parallel OpenAI requests
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
MAX_CONCURRENT_REQUESTS = 8  # Process 8 images simultaneously
BATCH_SIZE = 20  # Process in batches of 20
REQUEST_TIMEOUT = 60  # Timeout per request

# Set up logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('openai_food_image_generation_FAST.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

async def generate_image_with_openai_async(session: aiohttp.ClientSession, prompt: str, filename: str, semaphore: asyncio.Semaphore) -> Optional[bytes]:
    """Generate an image using OpenAI's DALL-E API asynchronously."""
    
    async with semaphore:  # Limit concurrent requests
        url = "https://api.openai.com/v1/images/generations"
        headers = {
            "Authorization": f"Bearer {OPENAI_API_KEY}",
            "Content-Type": "application/json"
        }
        
        # Optimized settings for speed and cost
        data = {
            "model": "dall-e-2",  # Faster and cheaper than dall-e-3
            "prompt": prompt,
            "n": 1,
            "size": "512x512",    # Smaller = faster and cheaper
            "quality": "standard",
            "response_format": "url"
        }
        
        try:
            async with session.post(url, headers=headers, json=data, timeout=REQUEST_TIMEOUT) as response:
                if response.status == 200:
                    result = await response.json()
                    image_url = result["data"][0]["url"]
                    
                    # Download the image
                    async with session.get(image_url, timeout=30) as img_response:
                        if img_response.status == 200:
                            image_data = await img_response.read()
                            return image_data
                        else:
                            logger.error(f"Failed to download image for {filename}: {img_response.status}")
                
                elif response.status == 429:  # Rate limit
                    logger.warning(f"Rate limited for {filename}, will retry...")
                    await asyncio.sleep(5)  # Wait before retry
                    return None
                    
                else:
                    error_text = await response.text()
                    logger.error(f"OpenAI API error for {filename}: {response.status} - {error_text}")
                    
        except asyncio.TimeoutError:
            logger.error(f"Request timeout for {filename}")
        except Exception as e:
            logger.error(f"Request failed for {filename}: {e}")
    
    return None

async def process_food_batch_async(foods_batch: List[Dict], collection) -> tuple:
    """Process a batch of foods concurrently."""
    
    if not OPENAI_API_KEY:
        logger.error("OpenAI API key not found. Set OPENAI_API_KEY environment variable.")
        return 0, 0
    
    # Setup async session and semaphore
    semaphore = asyncio.Semaphore(MAX_CONCURRENT_REQUESTS)
    timeout = aiohttp.ClientTimeout(total=REQUEST_TIMEOUT)
    
    success_count = 0
    error_count = 0
    
    async with aiohttp.ClientSession(timeout=timeout) as session:
        tasks = []
        food_data = []
        
        # Prepare all tasks
        for food in foods_batch:
            food_number = food["foodNumber"]
            primary_name = food["primary_name"]
            scientific_name = food["scientific_name"]
            
            if not primary_name:
                continue
            
            # Create filename
            scientific_clean = sanitize_filename(scientific_name) if scientific_name else "unknown"
            filename = f"{food_number}_{scientific_clean}.jpg"
            filepath = IMAGE_OUTPUT_DIR / filename
            
            # Skip if exists
            if filepath.exists():
                continue
            
            # Generate prompt (import from brand config)
            try:
                from brand_style_config import build_positive_prompt
                prompt = build_positive_prompt(primary_name, scientific_name)
            except ImportError:
                prompt = f"Simple flat vector illustration of {primary_name}, happy smiling character with friendly eyes, simple line art, flat design, minimal details, playful and inviting, soft rounded shapes, clean vector style, safe and friendly for children, solid color background"
            
            logger.info(f"🎨 Preparing OpenAI FAST batch: {primary_name} -> {filename}")
            logger.info(f"📝 Prompt: {prompt}")
            
            # Create async task
            task = generate_image_with_openai_async(session, prompt, filename, semaphore)
            tasks.append(task)
            food_data.append((food, filename, filepath))
        
        if not tasks:
            return 0, 0
        
        # Execute all tasks concurrently
        results = await asyncio.gather(*tasks, return_exceptions=True)
        
        # Process results
        for i, (result, (food, filename, filepath)) in enumerate(zip(results, food_data)):
            if isinstance(result, Exception):
                logger.error(f"Task failed for {filename}: {result}")
                error_count += 1
            elif result:  # Successfully got image data
                if save_image(result, filepath):
                    if update_database_record(collection, food["foodNumber"], filename):
                        success_count += 1
                    else:
                        error_count += 1
                else:
                    error_count += 1
            else:
                error_count += 1
    
    return success_count, error_count

async def main_async():
    """Main async function for parallel processing."""
    logger.info("🚀 Starting PARALLEL OpenAI food image generation...")
    
    if not OPENAI_API_KEY:
        logger.error("OpenAI API key not found. Please set OPENAI_API_KEY environment variable.")
        logger.info("Get an API key from: https://platform.openai.com/api-keys")
        return
    
    # Create output directory
    IMAGE_OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    
    # Connect to MongoDB
    try:
        client = pymongo.MongoClient(MONGODB_URI)
        db = client[DATABASE_NAME]
        collection = db[COLLECTION_NAME]
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
    
    logger.info(f"Processing {len(unique_foods)} foods with PARALLEL OpenAI requests...")
    logger.info(f"Concurrency: {MAX_CONCURRENT_REQUESTS} simultaneous requests")
    logger.info(f"Batch size: {BATCH_SIZE} foods per batch")
    
    # Process in batches
    total_success = 0
    total_errors = 0
    total_batches = (len(unique_foods) + BATCH_SIZE - 1) // BATCH_SIZE
    
    start_time = time.time()
    
    for batch_idx in range(0, len(unique_foods), BATCH_SIZE):
        batch = unique_foods[batch_idx:batch_idx + BATCH_SIZE]
        batch_num = (batch_idx // BATCH_SIZE) + 1
        
        logger.info(f"Processing batch {batch_num}/{total_batches} ({len(batch)} foods)...")
        
        batch_start = time.time()
        success, errors = await process_food_batch_async(batch, collection)
        batch_time = time.time() - batch_start
        
        total_success += success
        total_errors += errors
        
        foods_per_sec = len(batch) / batch_time if batch_time > 0 else 0
        logger.info(f"Batch {batch_num} complete: {success} success, {errors} errors, {foods_per_sec:.1f} foods/sec")
        
        # Small delay between batches to be API-friendly
        if batch_num < total_batches:
            await asyncio.sleep(1)
    
    total_time = time.time() - start_time
    
    # Final statistics
    logger.info("=" * 60)
    logger.info("⚡ PARALLEL OPENAI GENERATION COMPLETE")
    logger.info("=" * 60)
    logger.info(f"Total processing time: {total_time/60:.1f} minutes")
    logger.info(f"Average speed: {len(unique_foods)/total_time:.1f} foods per second")
    logger.info(f"Successfully generated: {total_success}")
    logger.info(f"Errors: {total_errors}")
    logger.info(f"Concurrency used: {MAX_CONCURRENT_REQUESTS} parallel requests")
    
    # Cost estimation
    cost_per_image = 0.020  # DALL-E 2 512x512 cost
    total_cost = total_success * cost_per_image
    logger.info(f"💰 Estimated cost: ${total_cost:.2f}")
    
    logger.info("=" * 60)

def main():
    """Main function wrapper."""
    asyncio.run(main_async())

if __name__ == "__main__":
    main()
