# 🚀 Manual Railway Deployment Guide

## Step 1: Login to Railway

```bash
railway login
```
This will open your browser for authentication.

## Step 2: Deploy Backend

```bash
cd backend

# Build the application
mvn clean package -DskipTests

# Initialize Railway project
railway init
# Choose: "Empty Project" and name it "selective-eating-backend"

# Deploy
railway up
```

**Save the backend URL!** It will look like: `https://selective-eating-backend-production-xxxx.up.railway.app`

## Step 3: Deploy Frontend

```bash
cd ../frontend

# Install dependencies
npm install

# Create production env file with your backend URL
echo "VITE_API_BASE_URL=https://your-backend-url.railway.app/api/v1" > .env.production
echo "VITE_APP_NAME=Selective Eating" >> .env.production

# Build the application
npm run build

# Initialize Railway project  
railway init
# Choose: "Empty Project" and name it "selective-eating-frontend"

# Deploy
railway up
```

## Step 4: Configure Backend Environment Variables

Go to Railway dashboard: https://railway.app/dashboard

Click on your **backend project** and add these environment variables:

```
SPRING_PROFILES_ACTIVE=prod
SPRING_DATA_MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/selective_eating
SPRING_DATA_MONGODB_DATABASE=selective_eating
JWT_ACCESS_SECRET=your-very-long-secret-key-at-least-32-characters-long
JWT_REFRESH_SECRET=your-different-very-long-secret-key-32-characters
JWT_ACCESS_TTL_SECONDS=900
JWT_REFRESH_TTL_SECONDS=604800
CORS_ALLOW_ORIGINS=https://your-frontend-url.railway.app
LOG_LEVEL=INFO
```

## Step 5: Test Your Deployment

- **Frontend**: `https://your-frontend.railway.app`
- **Backend Health**: `https://your-backend.railway.app/api/actuator/health`
- **API Documentation**: `https://your-backend.railway.app/api/swagger-ui.html`

## 🎯 Quick Commands Reference

```bash
railway login           # Login to Railway
railway status          # Check deployment status
railway logs           # View application logs
railway open           # Open project in browser
railway list           # List all your projects
```

---

## ⚠️ Important Notes

1. **MongoDB URI**: Replace with your actual Atlas connection string
2. **JWT Secrets**: Use long, random strings (32+ characters each)
3. **CORS Origins**: Must exactly match your frontend URL (no trailing slash)
4. **Wait 2-3 minutes** after setting environment variables for restart
5. **Check logs** if something doesn't work: `railway logs`
