#!/bin/bash
# Vercel deployment helper script

echo "🚀 Deploying Selective Eating Frontend to Vercel..."

# Check if Vercel CLI is installed
if ! command -v vercel &> /dev/null; then
    echo "❌ Vercel CLI not found. Installing..."
    npm install -g vercel
fi

echo "📁 Navigating to frontend directory..."
cd "$(dirname "$0")"

echo "📦 Installing dependencies..."
npm install

echo "🏗️ Building application..."
npm run build

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    
    # Check if .env.production exists
    if [ ! -f ".env.production" ]; then
        echo "⚠️  Creating .env.production file..."
        echo "VITE_API_BASE_URL=https://your-backend.railway.app/api/v1" > .env.production
        echo "VITE_APP_NAME=Selective Eating" >> .env.production
        echo "📝 Please update the backend URL in .env.production with your actual Railway URL"
    fi
    
    echo "🚀 Deploying to Vercel..."
    vercel --prod
    
    echo "✅ Deployment complete!"
    echo "💡 Next steps:"
    echo "   1. Update VITE_API_BASE_URL in Vercel dashboard with your Railway backend URL"
    echo "   2. Update CORS_ALLOW_ORIGINS in Railway with your Vercel frontend URL"
    echo "   3. Test your application!"
    echo ""
    echo "📖 See DEPLOYMENT_STEPS.md for detailed instructions"
else
    echo "❌ Build failed. Please check the errors above."
    exit 1
fi
