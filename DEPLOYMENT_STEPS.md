# 🚀 Step-by-Step Deployment Guide

## Step 1: MongoDB Atlas Setup ✅
1. Go to https://cloud.mongodb.com/
2. Create free cluster (M0 tier)
3. Create database user with read/write permissions
4. Allow access from anywhere (Network Access → 0.0.0.0/0)
5. Get connection string: `mongodb+srv://username:password@cluster.mongodb.net/selective_eating`

## Step 2: Deploy Backend to Railway

### 2a. Login to Railway
```bash
railway login
```
This will open your browser to authenticate.

### 2b. Initialize and Deploy
```bash
cd backend
railway init
# Choose: "Empty project" or "Deploy from GitHub repo"
# Follow prompts to create project

railway up
```

### 2c. Set Environment Variables in Railway Dashboard
Go to your Railway project dashboard and add these variables:

```
SPRING_PROFILES_ACTIVE=prod
SPRING_DATA_MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/selective_eating
SPRING_DATA_MONGODB_DATABASE=selective_eating
JWT_ACCESS_SECRET=your-very-long-secret-key-for-access-tokens-at-least-256-bits
JWT_REFRESH_SECRET=your-different-very-long-secret-key-for-refresh-tokens-256-bits
JWT_ACCESS_TTL_SECONDS=900
JWT_REFRESH_TTL_SECONDS=604800
CORS_ALLOW_ORIGINS=https://your-frontend.vercel.app
LOG_LEVEL=INFO
```

### 2d. Get Your Backend URL
After deployment, Railway will give you a URL like: `https://your-app-name.up.railway.app`

## Step 3: Deploy Frontend to Vercel

### 3a. Install Vercel CLI
```bash
npm install -g vercel
```

### 3b. Deploy Frontend
```bash
cd frontend
vercel login
vercel --prod
```

### 3c. Set Environment Variables in Vercel
In Vercel dashboard, add:
```
VITE_API_BASE_URL=https://your-backend.railway.app/api/v1
VITE_APP_NAME=Selective Eating
```

### 3d. Redeploy with Environment Variables
```bash
vercel --prod
```

## Step 4: Update CORS Settings

Go back to Railway dashboard and update `CORS_ALLOW_ORIGINS` with your Vercel URL:
```
CORS_ALLOW_ORIGINS=https://your-frontend.vercel.app
```

## Step 5: Test Everything

- Backend health: `https://your-backend.railway.app/api/actuator/health`
- API docs: `https://your-backend.railway.app/api/swagger-ui.html`  
- Frontend: `https://your-frontend.vercel.app`

---

## 🔧 Quick Commands Reference

**Railway Commands:**
```bash
railway status        # Check deployment status
railway logs          # View application logs  
railway shell         # Access deployment shell
railway variables     # Manage environment variables
```

**Vercel Commands:**
```bash
vercel ls            # List deployments
vercel logs          # View deployment logs
vercel env           # Manage environment variables
```

---

## ⚠️ Important Notes

1. **MongoDB Connection String**: Replace `username`, `password`, and `cluster` with your actual values
2. **JWT Secrets**: Use long, random strings (at least 32 characters each)
3. **CORS Origins**: Must match your exact frontend URL (no trailing slash)
4. **Railway takes ~2-3 minutes** to build and deploy your Spring Boot app
5. **Vercel deploys instantly** but may take a minute for global CDN propagation

---

## 🆘 Troubleshooting

**Backend won't start?**
- Check Railway logs: `railway logs`
- Verify MongoDB connection string
- Ensure all environment variables are set

**Frontend can't connect to backend?**
- Verify `VITE_API_BASE_URL` matches your Railway URL exactly
- Check CORS settings in Railway
- Open browser dev tools to see network errors

**CORS errors?**
- Update `CORS_ALLOW_ORIGINS` in Railway with your Vercel URL
- Make sure there's no trailing slash in the URL
