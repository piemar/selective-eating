#!/bin/bash

# Optimized script for Apple Silicon Macs with MPS support
# This version tries to use Metal Performance Shaders for better performance

echo "🚀 Starting Stable Diffusion WebUI (Apple Silicon Optimized)..."
echo "============================================================="

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

echo "🍎 Detected: Apple M1 Pro"
echo "🎯 Using Metal Performance Shaders (MPS) for acceleration"
echo "🎨 Launching WebUI with Apple Silicon optimizations..."
echo "✅ WebUI will be available at: http://localhost:7860"
echo "✅ API will be available at: http://localhost:7860/docs"
echo ""
echo "⚡ Performance Note: MPS should be much faster than CPU!"
echo "🔄 If MPS fails, the fallback start_webui.sh uses CPU mode"
echo ""
echo "Press Ctrl+C to stop the server"
echo ""

# Launch with MPS optimizations for Apple Silicon
# --skip-torch-cuda-test: Skip CUDA check (Apple Silicon doesn't have CUDA) 
# --precision half: Use half precision for better performance with MPS
# --opt-split-attention: Optimize attention computation for Apple Silicon
python launch.py --listen --api --port 7860 \
    --skip-torch-cuda-test \
    --precision half \
    --opt-split-attention \
    --enable-insecure-extension-access

echo ""
echo "🛑 WebUI has been stopped."
