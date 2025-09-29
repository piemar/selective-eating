#!/bin/bash

# Script to disable mock mode and use real backend
echo "🌐 Disabling Mock Mode..."
echo ""

# Remove or update .env.local to disable mock mode
if [ -f .env.local ]; then
    # Check if file contains VITE_USE_MOCK_DATA
    if grep -q "VITE_USE_MOCK_DATA" .env.local; then
        # Replace true with false
        sed -i '' 's/VITE_USE_MOCK_DATA=true/VITE_USE_MOCK_DATA=false/' .env.local
        echo "📝 Updated .env.local - set VITE_USE_MOCK_DATA=false"
    else
        # Add the false setting
        echo "VITE_USE_MOCK_DATA=false" >> .env.local
        echo "📝 Added VITE_USE_MOCK_DATA=false to .env.local"
    fi
else
    # Create new .env.local with false
    echo "VITE_USE_MOCK_DATA=false" > .env.local
    echo "📝 Created .env.local with VITE_USE_MOCK_DATA=false"
fi

echo ""
echo "✅ Mock mode disabled!"
echo "🔄 Please restart your development server:"
echo "   npm run dev"
echo ""
echo "⚠️  Make sure your backend is running:"
echo "   Backend: http://localhost:8083"
echo "   MongoDB: selective_eating_dev database"
echo ""
echo "ℹ️  To re-enable mock mode, run: ./enable-mock-mode.sh"
