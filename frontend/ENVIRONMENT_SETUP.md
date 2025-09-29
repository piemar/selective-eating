# Environment Configuration

## Required Environment Variables

Create a `.env.local` file in the frontend directory with the following variables:

```bash
# API Configuration
VITE_API_BASE_URL=http://localhost:8081/api/v1

# Application Configuration
VITE_APP_NAME=Selective Eating

# Mock Mode (for development without backend)
VITE_USE_MOCK_DATA=false

# Development Configuration
VITE_DEV_PORT=8080
VITE_PREVIEW_PORT=8080
```

## Environment Variable Descriptions

- **VITE_API_BASE_URL**: The base URL for the backend API. Defaults to `http://localhost:8081/api/v1`
- **VITE_APP_NAME**: The application name displayed in the UI. Defaults to "Selective Eating"
- **VITE_USE_MOCK_DATA**: Set to `true` to use mock data instead of real API calls. Defaults to `false`
- **VITE_DEV_PORT**: Port for the development server. Defaults to `8080`
- **VITE_PREVIEW_PORT**: Port for the preview server. Defaults to `8080`

## Production Configuration

For production deployment, set these environment variables in your hosting platform:

- **VITE_API_BASE_URL**: Your production backend URL (e.g., `https://your-backend.railway.app/api/v1`)
- **VITE_APP_NAME**: Your production app name
- **VITE_USE_MOCK_DATA**: Should be `false` in production

## Backend CORS Configuration

Make sure your backend's `CORS_ALLOW_ORIGINS` environment variable includes your frontend URL.
