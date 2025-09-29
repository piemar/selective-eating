#!/bin/bash

# Script to enable mock mode for development
echo "🎭 Enabling Mock Mode..."
echo ""

# Create .env.local with mock mode enabled
echo "VITE_USE_MOCK_DATA=true" > .env.local

echo "✅ Mock mode enabled!"
echo "📝 Created .env.local with VITE_USE_MOCK_DATA=true"
echo ""
echo "🔄 Please restart your development server:"
echo "   npm run dev"
echo ""
echo "🎯 What you can now test:"
echo "   • Food search works without backend"
echo "   • AI suggestions with realistic data"
echo "   • Category filtering and pagination"
echo "   • Multi-language support (EN/SV)"
echo "   • Realistic loading delays"
echo ""
echo "ℹ️  To disable mock mode, run: ./disable-mock-mode.sh"
