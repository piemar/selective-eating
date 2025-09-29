#!/bin/bash

# Simple script to start Stable Diffusion WebUI with correct environment
# This fixes the common "packaging module not found" error by ensuring
# the virtual environment is activated before launching.

echo "🚀 Starting Stable Diffusion WebUI..."
echo "====================================="

# Navigate to WebUI directory
cd "$(dirname "$0")/stable-diffusion-webui"

# Check if virtual environment exists
if [ ! -d "venv" ]; then
    echo "❌ Virtual environment not found!"
    echo "Please make sure you're in the correct directory and the WebUI is properly installed."
    exit 1
fi

echo "🔧 Activating virtual environment..."
source venv/bin/activate

echo "🎨 Launching WebUI with API enabled..."
echo "✅ WebUI will be available at: http://localhost:7860"
echo "✅ API will be available at: http://localhost:7860/docs"
echo ""
echo "Press Ctrl+C to stop the server"
echo ""

# Launch with recommended settings for food image generation on Apple Silicon
# --skip-torch-cuda-test: Skip CUDA check (Apple Silicon doesn't have CUDA)
# --no-half: Use full precision (better compatibility on Apple Silicon)
# --use-cpu all: Force CPU usage for maximum compatibility
python launch.py --listen --api --port 7860 --skip-torch-cuda-test --no-half --use-cpu all

echo ""
echo "🛑 WebUI has been stopped."
