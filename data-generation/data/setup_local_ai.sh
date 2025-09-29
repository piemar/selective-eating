#!/bin/bash

# Local AI Image Generation Setup Script
# This script helps you set up free local AI image generation

echo "🤖 Setting up FREE local AI image generation..."
echo "This will allow you to generate images without paying OpenAI!"
echo

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
echo "🔍 Checking prerequisites..."

if ! command_exists python3; then
    echo "❌ Python 3 is required but not installed."
    exit 1
fi

if ! command_exists git; then
    echo "❌ Git is required but not installed."
    exit 1
fi

echo "✅ Prerequisites met!"
echo

# Install Python dependencies
echo "📦 Installing Python dependencies..."
if command_exists pipenv; then
    echo "Using pipenv..."
    pipenv install
else
    echo "Using pip..."
    pip3 install -r requirements.txt diffusers transformers torch torchvision accelerate
fi

echo

# Present options
echo "🎨 Choose your local AI setup:"
echo "1. Automatic1111 Web UI (Recommended for beginners)"
echo "2. Direct Python integration (Diffusers library)"
echo "3. ComfyUI (Advanced users)"
echo "4. Fooocus (Simplest, one-click)"
echo

read -p "Enter your choice (1-4): " choice

case $choice in
    1)
        echo
        echo "🚀 Setting up Automatic1111 Stable Diffusion Web UI..."
        if [ ! -d "stable-diffusion-webui" ]; then
            echo "Downloading Automatic1111..."
            git clone https://github.com/AUTOMATIC1111/stable-diffusion-webui.git
        else
            echo "Automatic1111 already downloaded, updating..."
            cd stable-diffusion-webui && git pull && cd ..
        fi
        
        echo
        echo "✅ Automatic1111 setup complete!"
        echo
        echo "📋 Next steps:"
        echo "1. cd stable-diffusion-webui"
        echo "2. ./webui.sh --api    (IMPORTANT: --api flag is required!)"
        echo "3. Wait for models to download (first run)"
        echo "4. Test at: http://127.0.0.1:7860"
        echo "5. Run: pipenv run python generate_sample_images_local.py"
        ;;
    
    2)
        echo
        echo "🐍 Setting up Direct Python integration..."
        echo "This will download models automatically when first run."
        echo
        echo "✅ Python dependencies already installed!"
        echo
        echo "📋 Next steps:"
        echo "1. Edit generate_food_images_local.py"
        echo "2. Set: USE_DIFFUSERS = True"
        echo "3. Run: pipenv run python generate_sample_images_local.py"
        echo "   (First run will download ~4GB of models)"
        ;;
    
    3)
        echo
        echo "🎨 Setting up ComfyUI..."
        if [ ! -d "ComfyUI" ]; then
            echo "Downloading ComfyUI..."
            git clone https://github.com/comfyanonymous/ComfyUI.git
        else
            echo "ComfyUI already downloaded, updating..."
            cd ComfyUI && git pull && cd ..
        fi
        
        echo
        echo "✅ ComfyUI setup complete!"
        echo
        echo "📋 Next steps:"
        echo "1. cd ComfyUI"
        echo "2. pip install -r requirements.txt"
        echo "3. python main.py --api"
        echo "4. Download models to models/checkpoints/"
        echo "5. Configure API endpoint in generate_food_images_local.py"
        ;;
    
    4)
        echo
        echo "🎯 Setting up Fooocus..."
        if [ ! -d "Fooocus" ]; then
            echo "Downloading Fooocus..."
            git clone https://github.com/lllyasviel/Fooocus.git
        else
            echo "Fooocus already downloaded, updating..."
            cd Fooocus && git pull && cd ..
        fi
        
        echo
        echo "✅ Fooocus setup complete!"
        echo
        echo "📋 Next steps:"
        echo "1. cd Fooocus"
        echo "2. python launch.py"
        echo "3. Configure API endpoint in generate_food_images_local.py"
        ;;
    
    *)
        echo "Invalid choice. Please run the script again."
        exit 1
        ;;
esac

echo
echo "🎉 Setup complete!"
echo
echo "💡 Tips for better performance:"
echo "• Use GPU if available (much faster)"
echo "• For low VRAM: add --lowvram flag"
echo "• For CPU only: add --use-cpu all flag"
echo
echo "🧪 Test your setup:"
echo "pipenv run python generate_sample_images_local.py"
echo
echo "🚀 Generate all 2,569 images for FREE:"
echo "pipenv run python generate_food_images_local.py"
echo
echo "💰 Estimated savings vs OpenAI: $103 🎉"
