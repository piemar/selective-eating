# Food Image Generator

This directory contains scripts to generate child-friendly images for all foods in the selective eating database.

## Overview

The image generator connects to your local MongoDB database, retrieves all food records from the `selective_eating_dev.foods` collection, generates child-friendly cartoon images for each food, and updates the database records with the image filenames.

## 🎨 Two Generation Options

### 1. **OpenAI DALL-E** (Paid, High Quality)
- Uses OpenAI's DALL-E API
- Cost: ~$103 for all 2,569 images
- High quality, consistent results
- Fast generation (~2 sec/image)

### 2. **Local AI Models** (FREE! 🎉)
- Uses free open-source models like Stable Diffusion
- Cost: $0 (completely free!)
- Runs on your own hardware
- Customizable and private

## Files

### OpenAI Version (Paid)
- `generate_food_images.py` - Main script using OpenAI DALL-E
- `generate_sample_images.py` - Test script with 5 images (OpenAI)

### Local AI Version (FREE)
- `generate_food_images_local.py` - Main script using local AI models
- `generate_sample_images_local.py` - Test script with 3 images (local)
- `LOCAL_SETUP_GUIDE.md` - Complete setup guide for local AI
- `setup_local_ai.sh` - Automated setup script

### Brand Style Configuration
- `brand_style_config.py` - Centralized brand style settings
- `test_brand_style.py` - Preview your brand style prompts  
- `BRAND_CUSTOMIZATION_GUIDE.md` - How to customize your brand style

### Shared
- `test_connection.py` - MongoDB connection test
- `requirements.txt` - Python package requirements
- `Pipfile` / `Pipfile.lock` - Pipenv environment configuration

## Setup

### 1. Install Dependencies

Dependencies are already configured in the Pipfile. Install them with:

```bash
cd backend/data
pipenv install
```

### 2. Get OpenAI API Key

1. Go to [OpenAI API Keys](https://platform.openai.com/api-keys)
2. Create a new API key
3. Set it as an environment variable:

```bash
export OPENAI_API_KEY='sk-proj-your-key-here'
```

### 3. Verify MongoDB is Running

Make sure your MongoDB server is running on `mongodb://localhost:27017` with the `selective_eating_dev` database.

## Usage

### Test Connection (Recommended First Step)

```bash
cd backend/data
pipenv run python test_connection.py
```

This will verify your MongoDB connection and show sample data structure.

### Preview Your Brand Style (Recommended)

```bash
cd backend/data
pipenv run python test_brand_style.py
```

This shows you exactly what prompts will be used to generate your brand-consistent images:
- ✅ Simple, flat, line-based designs
- ✅ Playful touches (happy fruits, smiling vegetables)  
- ✅ Abstract food shapes, not realistic photos
- ✅ Safe and inviting for children

## Option 1: FREE Local AI Generation 🎉

### Quick Setup
```bash
cd backend/data
chmod +x setup_local_ai.sh
./setup_local_ai.sh
```

### Manual Setup
See `LOCAL_SETUP_GUIDE.md` for detailed instructions.

### Test Local Generation (3 images)
```bash
cd backend/data
pipenv run python generate_sample_images_local.py
```

### Generate All Images Locally (FREE!)
```bash
cd backend/data
pipenv run python generate_food_images_local.py
```

**💰 Cost: $0 (completely free!)**

## Option 2: OpenAI DALL-E (Paid)

### Test OpenAI Generation (5 images)
```bash
cd backend/data
export OPENAI_API_KEY='sk-proj-your-key-here'
pipenv run python generate_sample_images.py
```

### Generate All Images with OpenAI

⚠️ **Warning**: This will cost approximately $103 in OpenAI credits.

```bash
cd backend/data
pipenv run python generate_food_images.py
```

## Output

### Image Files

Images are saved in `backend/data/image/foods/` with the format:
- `{nummer}_{scientific_name}.jpg`
- Example: `1_Bos_taurus.jpg` for beef tallow

### Database Updates

Each food record in MongoDB is updated with an `imageUrl` field pointing to the generated image:
```json
{
  "imageUrl": "image/foods/1_Bos_taurus.jpg"
}
```

## Image Characteristics

- **Format**: JPG with high quality (95%)
- **Size**: 1024x1024 pixels
- **Style**: Child-friendly cartoon illustrations with bright, appealing colors
- **Content**: Foods are depicted as cute cartoon characters with friendly faces where appropriate
- **Background**: Simple backgrounds suitable for educational apps

## Cost Estimation

- OpenAI DALL-E 3: $0.040 per image (standard quality)
- Total for 2,569 unique foods: ~$103
- If you want to use DALL-E 2 (cheaper), modify the script to use "dall-e-2" model (~$0.020 per image = ~$51 total)

## Rate Limiting

The script includes rate limiting (1 second delay between requests) to avoid hitting OpenAI's API limits. Full generation will take approximately 45-60 minutes.

## Error Handling

- Failed API calls are retried up to 3 times
- All operations are logged to `food_image_generation.log`
- Script can be safely restarted - it skips existing images
- Database updates only occur after successful image generation

## Troubleshooting

### MongoDB Connection Issues
```bash
# Check if MongoDB is running
brew services start mongodb-community
# Or
mongod --config /usr/local/etc/mongod.conf

# Test connection manually
mongosh mongodb://localhost:27017/selective_eating_dev
```

### OpenAI API Issues
- Verify your API key is correctly set
- Check your OpenAI account has sufficient credits
- Monitor rate limits in the logs

### Image Generation Issues
- Check available disk space in `backend/data/image/foods/`
- Verify write permissions
- Check the log file for detailed error messages

## Monitoring Progress

The script shows progress bars and logs detailed information:
- Real-time progress bar with current food being processed
- Success/error counts
- Estimated time remaining
- Final summary statistics

## Next Steps

After running the image generator:

1. Verify images were created in `backend/data/image/foods/`
2. Check that database records were updated with `imageUrl` fields
3. Configure your Spring Boot application to serve images from this directory
4. Update your frontend to display the generated images
