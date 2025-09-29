#!/bin/bash
# Environment setup script for Selective Eating Frontend

echo "🔧 Setting up environment configuration..."

# Create .env.local file if it doesn't exist
if [ ! -f .env.local ]; then
    echo "📝 Creating .env.local file..."
    cat > .env.local << EOF
# API Configuration
VITE_API_BASE_URL=http://localhost:8081/api/v1

# Application Configuration
VITE_APP_NAME=Selective Eating

# Mock Mode (for development without backend)
VITE_USE_MOCK_DATA=false

# Development Configuration
VITE_DEV_PORT=8080
VITE_PREVIEW_PORT=8080
EOF
    echo "✅ Created .env.local file with default configuration"
else
    echo "⚠️  .env.local already exists, skipping creation"
fi

echo ""
echo "🌐 Current API Configuration:"
echo "   Backend URL: http://localhost:8081/api/v1"
echo "   Frontend URL: http://localhost:8080"
echo ""
echo "📖 To customize, edit the .env.local file"
echo "📚 See ENVIRONMENT_SETUP.md for detailed configuration options"
echo ""
echo "🚀 Ready to start development!"
