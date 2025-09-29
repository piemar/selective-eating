#!/bin/bash
# Static deployment script for Selective Eating Frontend

echo "🚀 Building Selective Eating Frontend for static deployment..."

# Navigate to frontend directory
cd "$(dirname "$0")"

echo "📦 Installing dependencies..."
npm install

echo "🏗️ Building application..."
npm run build

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo ""
    echo "📁 Static files are ready in the 'dist' directory"
    echo "📋 You can deploy these files to any static hosting service:"
    echo ""
    echo "🌐 Deployment Options:"
    echo "   1. Netlify: Drag and drop the 'dist' folder to netlify.com/drop"
    echo "   2. Vercel: Use 'vercel --prod' (after login)"
    echo "   3. GitHub Pages: Push 'dist' contents to gh-pages branch"
    echo "   4. Railway: Use 'railway up' (if you have available resources)"
    echo "   5. Any static hosting: Upload 'dist' folder contents"
    echo ""
    echo "📝 Environment Variables to set in your hosting platform:"
    echo "   VITE_API_BASE_URL=https://your-backend-url/api/v1"
    echo "   VITE_APP_NAME=Selective Eating"
    echo ""
    echo "🔗 Backend CORS Configuration:"
    echo "   Add your frontend URL to CORS_ALLOW_ORIGINS in backend"
    echo ""
    echo "📖 Files ready for deployment:"
    ls -la dist/
else
    echo "❌ Build failed. Please check the errors above."
    exit 1
fi
