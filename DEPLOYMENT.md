# Deployment Guide

## Recommended Architecture: Frontend on Vercel + Backend on Railway

### Frontend Deployment (Vercel)

1. **Connect to Vercel**:
   ```bash
   cd frontend
   npm install -g vercel
   vercel login
   vercel --prod
   ```

2. **Environment Variables in Vercel**:
   - Go to your Vercel project dashboard
   - Add environment variables:
     - `VITE_API_BASE_URL`: Your backend URL (e.g., `https://your-app-name.up.railway.app/api/v1`)
     - `VITE_APP_NAME`: Selective Eating

3. **Build Configuration**:
   - Vercel will automatically detect the Vite configuration
   - Build command: `npm run build`
   - Output directory: `dist`

### Backend Deployment (Railway - Recommended)

1. **Connect to Railway**:
   ```bash
   # Install Railway CLI
   npm install -g @railway/cli
   
   # Login and deploy
   railway login
   cd backend
   railway init
   railway up
   ```

2. **Environment Variables in Railway**:
   ```
   SPRING_PROFILES_ACTIVE=prod
   SPRING_DATA_MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/selective_eating
   SPRING_DATA_MONGODB_DATABASE=selective_eating
   JWT_ACCESS_SECRET=your-secret-key-here
   JWT_REFRESH_SECRET=your-refresh-secret-key-here
   JWT_ACCESS_TTL_SECONDS=900
   JWT_REFRESH_TTL_SECONDS=604800
   CORS_ALLOW_ORIGINS=https://your-frontend.vercel.app
   LOG_LEVEL=INFO
   PORT=8080
   ```

3. **Domain Configuration**:
   - Railway provides a domain like `your-app-name.up.railway.app`
   - Update your frontend's `VITE_API_BASE_URL` with this URL

### Alternative: Backend on Render

If you prefer Render over Railway:

1. **Connect to Render**:
   - Go to render.com and connect your GitHub repo
   - Select the `backend` directory
   - Build command: `mvn clean package -DskipTests`
   - Start command: `java -Dserver.port=$PORT -jar target/selective-eating-backend-0.0.1-SNAPSHOT.jar`

2. **Environment Variables** (same as Railway list above)

### Database Setup

1. **MongoDB Atlas**:
   - Create a cluster at mongodb.com
   - Whitelist your backend service IP
   - Get connection string for `SPRING_DATA_MONGODB_URI`

### Complete Deployment Steps

1. **Deploy Backend First**:
   ```bash
   cd backend
   railway up  # or deploy to Render
   ```

2. **Get Backend URL and Update Frontend**:
   ```bash
   # Update frontend/.env.production with backend URL
   echo "VITE_API_BASE_URL=https://your-backend.railway.app/api/v1" > frontend/.env.production
   ```

3. **Deploy Frontend**:
   ```bash
   cd frontend
   vercel --prod
   ```

4. **Update CORS Settings**:
   - Add your frontend Vercel URL to backend CORS_ALLOW_ORIGINS

### Health Checks

- **Frontend**: `https://your-frontend.vercel.app`
- **Backend**: `https://your-backend.railway.app/actuator/health`
- **API Documentation**: `https://your-backend.railway.app/swagger-ui.html`

### Monitoring and Logs

- **Vercel**: Check deployment logs in Vercel dashboard
- **Railway**: Use `railway logs` command or dashboard
- **MongoDB**: Monitor in Atlas dashboard

### Cost Considerations

- **Vercel**: Free tier covers most frontend needs
- **Railway**: $5/month for backend (free tier available)
- **MongoDB Atlas**: Free tier (512MB) available
- **Total**: ~$5/month for production deployment
