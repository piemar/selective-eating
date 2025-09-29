import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Sparkles, Heart, Search, Database, Loader2 } from 'lucide-react';
import FoodCard from '@/components/FoodCard';
import { Button } from '@/components/ui/button';
import { useQuery } from '@tanstack/react-query';
import { api, Food, getFoodImageUrl } from '@/lib/api';

// Import hero image
import heroImage from '@/assets/hero-families.png';

export default function Home() {
  const [selectedFoods, setSelectedFoods] = useState<number[]>([]);
  const navigate = useNavigate();

  // Get popular foods for quick start
  const { data: popularFoods, isLoading: isLoadingPopularFoods, error: popularFoodsError } = useQuery<Food[]>({
    queryKey: ['popular-foods'],
    queryFn: async () => {
      console.log('🔍 Fetching foods for quick start...');
      // Get a mix of English foods from different categories
      const englishFoods = await api.getFoodsByLanguage('en');
      console.log('📊 Total English foods received:', englishFoods.length);
      
      // Filter for child-friendly foods and take first 6
      const filtered = englishFoods
        .filter(food => {
          // Ensure name and foodCategory are not null/undefined
          const name = food.name?.toLowerCase() || '';
          const category = food.foodCategory?.toLowerCase() || '';
          
          return (
            name.includes('apple') ||
            name.includes('banana') ||
            name.includes('pasta') ||
            name.includes('rice') ||
            name.includes('chicken') ||
            name.includes('cheese') ||
            name.includes('bread') ||
            name.includes('milk') ||
            category.includes('fruit') ||
            category.includes('dairy')
          );
        })
        .slice(0, 6);
      
      console.log('🎯 Filtered foods for quick start:', filtered.length, filtered.map(f => f.name));
      return filtered;
    },
    staleTime: 10 * 60 * 1000, // 10 minutes
  });

  const toggleFood = (foodNumber: number) => {
    setSelectedFoods(prev => 
      prev.includes(foodNumber) 
        ? prev.filter(id => id !== foodNumber)
        : [...prev, foodNumber]
    );
  };

  const handleGetSuggestions = () => {
    if (selectedFoods.length > 0 && popularFoods) {
      const selectedFoodObjects = popularFoods.filter(food => 
        selectedFoods.includes(food.foodNumber)
      );
      navigate('/suggestions', { state: { selectedFoods: selectedFoodObjects } });
    }
  };

  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <div className="px-6 py-12 max-w-4xl mx-auto text-center">
        <div className="mb-8">
          <img 
            src={heroImage} 
            alt="Families enjoying meals together"
            className="w-full max-w-md mx-auto mb-6 rounded-3xl"
          />
        </div>
        
        <h1 className="text-4xl font-bold text-foreground mb-4">
          FoodBridge
        </h1>
        <p className="text-xl text-muted-foreground mb-6 max-w-2xl mx-auto leading-relaxed">
          A gentle, AI-powered companion helping families navigate selective eating with confidence and joy
        </p>
        
        <div className="flex items-center justify-center gap-2 text-success mb-8">
          <Heart className="w-5 h-5 fill-current" />
          <span className="text-sm font-medium">Trusted by thousands of families</span>
        </div>
      </div>

      {/* Food Selection Options */}
      <div className="px-6 pb-6 max-w-4xl mx-auto">
        <div className="text-center mb-8">
          <h2 className="text-3xl font-bold text-foreground mb-2">
            What does your child enjoy eating? 🌟
          </h2>
          <p className="text-muted-foreground text-lg">
            Choose how you'd like to select foods your child already enjoys
          </p>
        </div>

        {/* Selection Options */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
          {/* Quick Selection */}
          <div className="bg-card rounded-3xl p-6 shadow-gentle">
            <div className="text-center mb-4">
              <Heart className="w-12 h-12 text-primary mx-auto mb-3" />
              <h3 className="text-xl font-semibold text-foreground mb-2">
                Quick Start
              </h3>
              <p className="text-muted-foreground">
                Pick from popular foods to get started quickly
              </p>
            </div>
            
            {/* Loading state */}
            {isLoadingPopularFoods && (
              <div className="flex items-center justify-center py-8">
                <Loader2 className="w-6 h-6 animate-spin text-primary mr-2" />
                <span className="text-muted-foreground">Loading popular foods...</span>
              </div>
            )}

            {/* Popular foods grid */}
            {popularFoods && popularFoods.length > 0 && (
              <div className="grid grid-cols-2 gap-3 mb-4">
                {popularFoods.map((food) => (
                  <div key={food.foodNumber} className="scale-90">
                    <img
                      src={getFoodImageUrl(food)}
                      alt={food.name}
                      className={`w-full h-20 object-cover rounded-2xl mb-2 cursor-pointer transition-all ${
                        selectedFoods.includes(food.foodNumber)
                          ? 'ring-2 ring-primary ring-offset-2'
                          : 'hover:ring-2 hover:ring-primary/50'
                      }`}
                      onClick={() => toggleFood(food.foodNumber)}
                      onError={(e) => {
                        const target = e.target as HTMLImageElement;
                        target.src = `https://via.placeholder.com/160x80/f3f4f6/9ca3af?text=${encodeURIComponent(food.name.charAt(0).toUpperCase())}`;
                      }}
                    />
                    <h4 className="text-sm font-medium text-foreground text-center">
                      {food.name}
                    </h4>
                    <p className="text-xs text-muted-foreground text-center">
                      {food.foodCategory}
                    </p>
                  </div>
                ))}
              </div>
            )}

            {/* Error state */}
            {popularFoodsError && (
              <div className="text-center py-8 text-red-500">
                <p>Error loading popular foods:</p>
                <p className="text-sm">{popularFoodsError.message}</p>
                <p className="text-xs mt-2">Check console for details</p>
              </div>
            )}

            {/* No foods fallback */}
            {!isLoadingPopularFoods && !popularFoodsError && (!popularFoods || popularFoods.length === 0) && (
              <div className="text-center py-8 text-muted-foreground">
                <p>Unable to load popular foods.</p>
                <p className="text-sm">Try the search option instead!</p>
              </div>
            )}

            {/* Debug info in development */}
            {process.env.NODE_ENV === 'development' && (
              <div className="text-xs text-gray-400 mt-2">
                <div>Loading: {isLoadingPopularFoods.toString()}</div>
                <div>Foods count: {popularFoods?.length || 0}</div>
                <div>Error: {popularFoodsError?.message || 'none'}</div>
              </div>
            )}

            {selectedFoods.length > 0 && (
              <Button
                onClick={handleGetSuggestions}
                className="w-full font-semibold"
                size="lg"
              >
                <Sparkles className="w-4 h-4 mr-2" />
                Get Suggestions ({selectedFoods.length})
              </Button>
            )}
          </div>

          {/* Full Database Search */}
          <div className="bg-card rounded-3xl p-6 shadow-gentle">
            <div className="text-center mb-4">
              <Search className="w-12 h-12 text-primary mx-auto mb-3" />
              <h3 className="text-xl font-semibold text-foreground mb-2">
                Search All Foods
              </h3>
              <p className="text-muted-foreground">
                Search through 5,000+ foods from our comprehensive database
              </p>
            </div>
            
            <div className="space-y-4">
              <div className="bg-secondary/20 rounded-2xl p-4">
                <div className="flex items-center gap-2 text-sm text-muted-foreground mb-2">
                  <Database className="w-4 h-4" />
                  <span>Features:</span>
                </div>
                <ul className="text-sm text-muted-foreground space-y-1">
                  <li>• Search by name</li>
                  <li>• Filter by category</li>
                  <li>• Multiple languages</li>
                  <li>• Real food images</li>
                </ul>
              </div>
              
              <Button
                onClick={() => navigate('/food-selector')}
                variant="outline"
                className="w-full font-semibold"
                size="lg"
              >
                <Search className="w-4 h-4 mr-2" />
                Search Foods Database
              </Button>
            </div>
          </div>
        </div>

        {/* Info Section */}
        <div className="text-center">
          <p className="text-sm text-muted-foreground">
            Both options use AI to suggest similar foods your child might enjoy
          </p>
        </div>
      </div>
    </div>
  );
}