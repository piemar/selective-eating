#!/bin/bash
# Deploy both frontend and backend to Railway (single platform solution)

echo "🚀 Deploying Selective Eating (Frontend + Backend) to Railway..."

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

echo "🎯 This will create TWO Railway projects:"
echo "   1. Frontend (React + Vite)"
echo "   2. Backend (Spring Boot)"
echo ""
read -p "Continue? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "❌ Deployment cancelled"
    exit 1
fi

# Deploy Backend First
echo "🏗️ Step 1: Deploying Backend..."
cd backend

echo "   📦 Building Spring Boot application..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Backend build failed!"
    exit 1
fi

echo "   🚀 Creating Railway project for backend..."
railway init --name "selective-eating-backend"
railway up

echo "   ✅ Backend deployed!"
BACKEND_URL=$(railway status --json | jq -r '.deployments[0].url' 2>/dev/null || echo "https://your-backend.railway.app")
echo "   🌐 Backend URL: $BACKEND_URL"

cd ..

# Deploy Frontend Second
echo ""
echo "🎨 Step 2: Deploying Frontend..."
cd frontend

echo "   📦 Installing frontend dependencies..."
npm install

# Create production environment file with backend URL
echo "VITE_API_BASE_URL=$BACKEND_URL/api/v1" > .env.production
echo "VITE_APP_NAME=Selective Eating" >> .env.production

echo "   🏗️ Building React application..."
npm run build

if [ $? -ne 0 ]; then
    echo "❌ Frontend build failed!"
    exit 1
fi

echo "   🚀 Creating Railway project for frontend..."
railway init --name "selective-eating-frontend"
railway up

echo "   ✅ Frontend deployed!"
FRONTEND_URL=$(railway status --json | jq -r '.deployments[0].url' 2>/dev/null || echo "https://your-frontend.railway.app")
echo "   🌐 Frontend URL: $FRONTEND_URL"

cd ..

echo ""
echo "🎉 Deployment Complete!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🌐 Frontend: $FRONTEND_URL"
echo "🔧 Backend:  $BACKEND_URL"
echo "📊 API Docs: $BACKEND_URL/api/swagger-ui.html"
echo "❤️  Health:  $BACKEND_URL/api/actuator/health"
echo ""
echo "📋 Next Steps:"
echo "   1. Set environment variables in Railway dashboard for backend:"
echo "      SPRING_PROFILES_ACTIVE=prod"
echo "      SPRING_DATA_MONGODB_URI=mongodb+srv://..."
echo "      JWT_ACCESS_SECRET=your-secret-key"
echo "      CORS_ALLOW_ORIGINS=$FRONTEND_URL"
echo ""
echo "   2. Wait 2-3 minutes for deployment to be fully ready"
echo "   3. Test your app at $FRONTEND_URL"
echo ""
echo "🔗 Railway Dashboard: https://railway.app/dashboard"
