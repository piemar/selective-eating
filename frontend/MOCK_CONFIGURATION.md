# Mock Data Configuration

This guide explains how to configure the frontend to use mock/mockup data instead of the real backend API.

## Quick Start

To enable mock data mode, create a `.env.local` file in the `frontend/` directory with:

```env
VITE_USE_MOCK_DATA=true
```

## Configuration Options

### Environment Variables

Create a `.env.local` file in the `frontend/` directory with the following options:

```env
# Mock Data Configuration
VITE_USE_MOCK_DATA=true          # Enable mock data (true/false)

# API Configuration (when using real backend)
VITE_API_BASE_URL=http://localhost:8083/api/api/v1

# Development Settings
VITE_REACT_DEVTOOLS=true         # Enable React DevTools
```

### When to Use Mock Data

✅ **Use mock data when:**
- Backend server is not available
- Developing frontend features independently
- Creating demos or presentations
- Testing frontend functionality
- Working offline

❌ **Don't use mock data when:**
- Testing full integration
- Working with real user data
- Testing API error handling
- Performance testing

## What's Mocked

### Data Types
- ✅ Foods (17+ sample foods with English/Swedish variants)
- ✅ Food categories (14 realistic categories)
- ✅ AI suggestions (4 sample suggestions with reasons)
- ✅ User experiences (meal logging)
- ✅ User statistics (progress tracking)
- ✅ Search functionality
- ✅ Filtering and pagination

### Realistic Features
- 🔄 **API delays** (200-800ms to simulate real network)
- 🌍 **Multi-language** (English and Swedish foods)
- 📊 **Smart suggestions** (based on selected liked foods)
- 🎯 **Contextual filtering** (animal vs plant-based foods)

## Sample Mock Data

### Foods Available
- **Dairy**: Whey cheese, cottage cheese variants, milk
- **Fruits**: Apple (with/without skin), banana, pineapple
- **Proteins**: Grilled chicken breast
- **Carbs**: Pasta, rice, white bread
- **Categories**: 14+ realistic food categories

### Mock Suggestions
When you select foods and request AI suggestions, you'll get:
- Personalized recommendations based on your selections
- Realistic confidence scores (75-90%)
- Kid-friendly explanations
- Visual food images

## Switching Modes

### Enable Mock Mode
```bash
# Create/edit .env.local
echo "VITE_USE_MOCK_DATA=true" > .env.local

# Restart the development server
npm run dev
```

### Disable Mock Mode (Use Real Backend)
```bash
# Remove mock configuration
echo "VITE_USE_MOCK_DATA=false" > .env.local
# OR delete the .env.local file entirely

# Restart the development server  
npm run dev
```

## Development Tips

### Console Indicators
When mock mode is enabled, you'll see:
- Simulated loading times
- Mock data responses in network tab
- All functionality works without backend

### Debugging
- Check browser console for mock data logs
- Network tab shows no real API calls
- All user interactions work normally

### Customization
To modify mock data, edit `src/lib/mockData.ts`:
- Add more foods to `mockFoods` array
- Update categories in `mockCategories`
- Modify suggestions in `mockSuggestions`
- Adjust API delays in `simulateApiDelay()`

## Troubleshooting

**Mock mode not working?**
1. Check `.env.local` file exists in `frontend/` directory
2. Restart development server after creating `.env.local`
3. Verify `VITE_USE_MOCK_DATA=true` (not `"true"`)

**Still seeing backend errors?**
1. Ensure no typos in environment variable name
2. Clear browser cache and restart server
3. Check that `.env.local` is not in `.gitignore`

**Performance issues?**
1. Mock delays can be adjusted in `mockData.ts`
2. Reduce `simulateApiDelay()` values for faster responses
3. Mock mode should be faster than real backend calls
