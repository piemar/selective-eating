#!/bin/bash
# Clean Automatic1111 launch for food image generation
cd stable-diffusion-webui

echo "🚀 Starting Automatic1111 for food image generation..."
echo "🎯 Optimized for: focused, clear food images"
echo "⚡ Extensions: minimal set for stability"

./webui.sh \
    --api \
    --xformers \
    --opt-split-attention \
    --no-gradio-queue \
    --skip-install \
    --disable-console-progressbars
