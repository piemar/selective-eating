import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { ArrowLeft, ThumbsUp, ThumbsDown, MessageSquare, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import FoodCard from '@/components/FoodCard';
import { useQuery } from '@tanstack/react-query';
import { 
  api, 
  FoodSuggestion, 
  getUserId,
  getSuggestionImageUrl 
} from '@/lib/api';
import { useToast } from '@/hooks/use-toast';

export default function Suggestions() {
  const location = useLocation();
  const navigate = useNavigate();
  const { toast } = useToast();
  const selectedFoods = location.state?.selectedFoods || [];
  
  // Get suggestions based on selected foods or popular suggestions if none selected
  const { data: suggestions, isLoading, error } = useQuery<FoodSuggestion[]>({
    queryKey: ['suggestions', selectedFoods],
    queryFn: async () => {
      if (selectedFoods.length > 0) {
        // Handle both cases: food numbers array or food objects array
        const likedFoodNumbers = selectedFoods.map((food: any) => {
          // If it's already a number (from FoodSelector), use it directly
          if (typeof food === 'number') {
            return food;
          }
          // If it's a food object (from Home), get the foodNumber
          return food.foodNumber;
        }).filter(num => num != null); // Remove any null/undefined values
        
        console.log('🎯 Liked food numbers for suggestions:', likedFoodNumbers);
        return api.getPersonalizedSuggestions(likedFoodNumbers, 4);
      } else {
        // Get popular suggestions for new users
        return api.getPopularSuggestions(4);
      }
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
  });

  const handleTryFood = async (suggestion: FoodSuggestion, rating: number) => {
    try {
      // Log the food experience
      await api.logFoodExperience({
        userId: getUserId(),
        foodNumber: suggestion.foodNumber,
        foodName: suggestion.foodName,
        rating: rating,
        notes: rating >= 4 ? "Child liked this suggestion!" : "Child wasn't interested in this suggestion",
        context: "suggestion"
      });

      toast({
        title: rating >= 4 ? "Great choice! 🎉" : "Thanks for trying! 👍",
        description: rating >= 4 
          ? "We'll suggest more foods like this one!" 
          : "We'll learn from this to suggest better foods next time.",
      });

      // Navigate to logging page for more detailed feedback
      navigate('/log', { 
        state: { 
          foodNumber: suggestion.foodNumber,
          foodName: suggestion.foodName,
          initialRating: rating
        } 
      });
    } catch (error) {
      console.error('Error logging food experience:', error);
      toast({
        title: "Something went wrong",
        description: "We couldn't save your feedback, but you can still try the food!",
        variant: "destructive",
      });
    }
  };

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <div className="flex items-center mb-6">
        <Button
          variant="ghost"
          onClick={() => navigate(-1)}
          className="mr-4 p-2"
        >
          <ArrowLeft className="w-5 h-5" />
        </Button>
        <h1 className="text-2xl font-bold text-foreground">
          AI Food Suggestions ✨
        </h1>
      </div>

      <div className="mb-8">
        <p className="text-muted-foreground text-lg text-center">
          {selectedFoods.length > 0 
            ? "Based on your child's preferences, here are some similar foods to try:"
            : "Here are some popular foods that children often enjoy:"
          }
        </p>
      </div>

      {/* Loading State */}
      {isLoading && (
        <div className="flex items-center justify-center py-12">
          <Loader2 className="w-8 h-8 animate-spin text-primary" />
          <span className="ml-2 text-muted-foreground">Getting personalized suggestions...</span>
        </div>
      )}

      {/* Error State */}
      {error && (
        <div className="text-center py-12">
          <p className="text-muted-foreground mb-4">
            Sorry, we couldn't load suggestions right now.
          </p>
          <Button onClick={() => navigate('/')} variant="outline">
            Go Back Home
          </Button>
        </div>
      )}

      {/* Suggestions List */}
      {suggestions && !isLoading && (
        <div className="space-y-6">
          {suggestions.map((suggestion) => (
            <div key={suggestion.foodNumber} className="bg-card rounded-3xl p-6 shadow-gentle">
              <div className="flex flex-col md:flex-row gap-6">
                <div className="flex-shrink-0">
                  <div className="w-48 mx-auto md:mx-0">
                    <img
                      src={getSuggestionImageUrl(suggestion)}
                      alt={suggestion.foodName}
                      className="w-full h-32 object-cover rounded-2xl mb-3"
                      onError={(e) => {
                        const target = e.target as HTMLImageElement;
                        target.src = `https://via.placeholder.com/192x128/f3f4f6/9ca3af?text=${encodeURIComponent(suggestion.foodName.charAt(0).toUpperCase())}`;
                      }}
                    />
                    <div className="flex flex-wrap gap-1 justify-center">
                      {suggestion.tags.map((tag) => (
                        <span 
                          key={tag}
                          className="px-2 py-1 bg-primary/10 text-primary text-xs rounded-full"
                        >
                          {tag}
                        </span>
                      ))}
                    </div>
                  </div>
                </div>
                
                <div className="flex-1">
                  <div className="mb-4">
                    <h3 className="text-xl font-semibold text-foreground mb-2">
                      Why try {suggestion.foodName}?
                    </h3>
                    <p className="text-muted-foreground">
                      {suggestion.reason}
                    </p>
                    <div className="mt-2 text-sm text-muted-foreground">
                      Confidence: {Math.round(suggestion.confidenceScore * 100)}%
                    </div>
                  </div>

                  <div className="flex gap-3">
                    <Button
                      onClick={() => handleTryFood(suggestion, 5)}
                      variant="default"
                      className="flex-1 bg-success hover:bg-success/90"
                    >
                      <ThumbsUp className="w-4 h-4 mr-2" />
                      They loved it!
                    </Button>
                    
                    <Button
                      onClick={() => handleTryFood(suggestion, 3)}
                      variant="secondary"
                      className="flex-1"
                    >
                      It was okay
                    </Button>
                    
                    <Button
                      onClick={() => handleTryFood(suggestion, 1)}
                      variant="outline"
                      className="flex-1"
                    >
                      <ThumbsDown className="w-4 h-4 mr-2" />
                      Not interested
                    </Button>
                    
                    <Button
                      onClick={() => navigate('/log', { 
                        state: { 
                          foodNumber: suggestion.foodNumber,
                          foodName: suggestion.foodName
                        } 
                      })}
                      variant="ghost"
                    >
                      <MessageSquare className="w-4 h-4" />
                    </Button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      <div className="mt-8 text-center">
        <Button
          onClick={() => navigate('/')}
          variant="outline"
          size="lg"
        >
          Try More Foods
        </Button>
      </div>
    </div>
  );
}