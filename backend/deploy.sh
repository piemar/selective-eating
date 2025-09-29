#!/bin/bash
# Railway deployment helper script

echo "🚀 Deploying Selective Eating Backend to Railway..."

# Check if Railway CLI is installed
if ! command -v railway &> /dev/null; then
    echo "❌ Railway CLI not found. Installing..."
    npm install -g @railway/cli
fi

# Check if logged in to Railway
if ! railway whoami &> /dev/null; then
    echo "🔐 Please login to Railway..."
    railway login
fi

echo "📁 Navigating to backend directory..."
cd "$(dirname "$0")"

echo "🏗️ Building application..."
./mvnw clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo "🚀 Deploying to Railway..."
    railway up
    
    echo "🌐 Getting deployment URL..."
    railway status
    
    echo "✅ Deployment complete!"
    echo "💡 Next steps:"
    echo "   1. Set environment variables in Railway dashboard"
    echo "   2. Get your backend URL from Railway"
    echo "   3. Deploy frontend with the backend URL"
    echo ""
    echo "📖 See DEPLOYMENT_STEPS.md for detailed instructions"
else
    echo "❌ Build failed. Please check the errors above."
    exit 1
fi
