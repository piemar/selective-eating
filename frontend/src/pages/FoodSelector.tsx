import React, { useState, useEffect, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { Search, Filter, Sparkles, Heart, Loader2, ArrowLeft, X, Database } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import FoodCard from '@/components/FoodCard';
import { api, Food, formatFoodName, getFoodTags, getFoodImageUrl } from '@/lib/api';
import { useToast } from '@/hooks/use-toast';

// Import fallback food images
import bananaImage from '@/assets/food-banana.png';
import pastaImage from '@/assets/food-pasta.png';
import appleImage from '@/assets/food-apple.png';
import carrotImage from '@/assets/food-carrot.png';

const fallbackImages = [bananaImage, pastaImage, appleImage, carrotImage];
const getFallbackImage = (index: number) => fallbackImages[index % fallbackImages.length];

export default function FoodSelector() {
  const navigate = useNavigate();
  const { toast } = useToast();
  
  // State management
  const [selectedFoods, setSelectedFoods] = useState<number[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [selectedLanguage, setSelectedLanguage] = useState<'en' | 'sv' | 'all'>('en');
  const [showAllCategories, setShowAllCategories] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage] = useState(12); // Show 12 items per page

  // Fetch data with React Query
  const { data: categories = [], isLoading: categoriesLoading } = useQuery({
    queryKey: ['categories'],
    queryFn: api.getCategories,
  });

  const { data: allFoods = [], isLoading: foodsLoading, error } = useQuery({
    queryKey: ['foods', selectedLanguage],
    queryFn: () => {
      if (selectedLanguage === 'all') {
        // Get both languages
        return Promise.all([
          api.getFoodsByLanguage('en'),
          api.getFoodsByLanguage('sv')
        ]).then(([enFoods, svFoods]) => [...enFoods, ...svFoods]);
      } else {
        return api.getFoodsByLanguage(selectedLanguage);
      }
    },
    enabled: true,
  });

  // Search functionality
  const { data: searchResults = [], isLoading: searchLoading } = useQuery({
    queryKey: ['search', searchTerm],
    queryFn: () => api.searchFoods(searchTerm),
    enabled: searchTerm.length > 2,
  });

  // Filter and get all foods (without pagination)
  const allFilteredFoods = useMemo(() => {
    let foods = searchTerm.length > 2 ? searchResults : allFoods;

    // Only filter by category when not searching
    if (searchTerm.length <= 2 && selectedCategory !== 'all') {
      foods = foods.filter(food => food.foodCategory === selectedCategory);
    }

    // Remove duplicates based on foodNumber (for when both languages are selected)
    const uniqueFoods = foods.reduce((acc: Food[], food) => {
      const exists = acc.find(f => f.foodNumber === food.foodNumber);
      if (!exists) {
        acc.push(food);
      }
      return acc;
    }, []);

    return uniqueFoods;
  }, [allFoods, searchResults, searchTerm, selectedCategory]);

  // Pagination calculations
  const totalPages = Math.ceil(allFilteredFoods.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const displayFoods = allFilteredFoods.slice(startIndex, endIndex);

  // Handle food selection
  const toggleFood = (foodNumber: number) => {
    setSelectedFoods(prev => 
      prev.includes(foodNumber) 
        ? prev.filter(id => id !== foodNumber)
        : [...prev, foodNumber]
    );
  };

  // Handle suggestions navigation
  const handleGetSuggestions = () => {
    if (selectedFoods.length > 0) {
      // Get selected food objects
      const selectedFoodObjects = displayFoods.filter(food => 
        selectedFoods.includes(food.foodNumber)
      );
      
      console.log('🚀 Navigating to suggestions with:', {
        selectedFoodNumbers: selectedFoods,
        selectedFoodObjects: selectedFoodObjects.map(f => ({ foodNumber: f.foodNumber, name: f.name }))
      });
      
      navigate('/suggestions', { 
        state: { 
          selectedFoods: selectedFoods, // Pass food numbers directly for suggestions API
          selectedFoodObjects: selectedFoodObjects
        } 
      });
    } else {
      toast({
        title: "No foods selected",
        description: "Please select at least one food that your child enjoys!",
        variant: "destructive",
      });
    }
  };

  // Handle pagination
  const goToPage = (page: number) => {
    if (page >= 1 && page <= totalPages) {
      setCurrentPage(page);
      // Scroll to top when changing pages
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  const goToPreviousPage = () => {
    if (currentPage > 1) {
      goToPage(currentPage - 1);
    }
  };

  const goToNextPage = () => {
    if (currentPage < totalPages) {
      goToPage(currentPage + 1);
    }
  };

  // Handle search
  const handleSearch = (value: string) => {
    setSearchTerm(value);
    setCurrentPage(1); // Reset to first page when searching
    // Reset category filter when starting a search
    if (value.length > 2 && selectedCategory !== 'all') {
      setSelectedCategory('all');
    }
  };

  // Reset to first page when category changes
  const handleCategoryChange = (category: string) => {
    setSelectedCategory(category);
    setCurrentPage(1);
  };

  // Error handling
  useEffect(() => {
    if (error) {
      toast({
        title: "Connection Error",
        description: "Unable to load foods. Please check if the backend is running.",
        variant: "destructive",
      });
    }
  }, [error, toast]);

  const isLoading = foodsLoading || categoriesLoading;

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <div className="px-6 py-6 max-w-6xl mx-auto">
        <div className="flex items-center mb-6">
          <Button
            variant="ghost"
            onClick={() => navigate(-1)}
            className="mr-4 p-2"
          >
            <ArrowLeft className="w-5 h-5" />
          </Button>
          <div>
            <h1 className="text-3xl font-bold text-foreground">
              What does your child enjoy eating? 🍎
            </h1>
            <p className="text-muted-foreground text-lg mt-2">
              Search and select from {allFoods.length.toLocaleString()} foods to get personalized suggestions!
            </p>
          </div>
        </div>

        {/* Filters and Search */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
          {/* Search */}
          <div className="relative md:col-span-2">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground w-4 h-4" />
            <Input
              placeholder="Search for foods (e.g., apple, pasta, chicken)..."
              value={searchTerm}
              onChange={(e) => handleSearch(e.target.value)}
              className="pl-10 pr-10"
            />
            {searchLoading ? (
              <Loader2 className="absolute right-3 top-1/2 transform -translate-y-1/2 text-muted-foreground w-4 h-4 animate-spin" />
            ) : searchTerm && (
              <button
                onClick={() => setSearchTerm('')}
                className="absolute right-3 top-1/2 transform -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
              >
                <X className="w-4 h-4" />
              </button>
            )}
          </div>

          {/* Language Filter */}
          <Select value={selectedLanguage} onValueChange={(value: 'en' | 'sv' | 'all') => setSelectedLanguage(value)}>
            <SelectTrigger>
              <SelectValue placeholder="Language" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="en">English</SelectItem>
              <SelectItem value="sv">Svenska</SelectItem>
              <SelectItem value="all">Both Languages</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {/* Category Filter - Only show when not searching */}
        {searchTerm.length <= 2 && (
          <div className="mb-6">
            <div className="flex items-center gap-2 mb-3">
              <Filter className="w-4 h-4 text-muted-foreground" />
              <span className="text-sm font-medium text-muted-foreground">Filter by category:</span>
            </div>
            <div className="flex flex-wrap gap-2">
              <Badge
                variant={selectedCategory === 'all' ? 'default' : 'secondary'}
                className="cursor-pointer"
                onClick={() => handleCategoryChange('all')}
              >
                All Categories
              </Badge>
              {(showAllCategories ? categories : categories.slice(0, 10)).map((category) => (
                <Badge
                  key={category}
                  variant={selectedCategory === category ? 'default' : 'secondary'}
                  className="cursor-pointer"
                  onClick={() => handleCategoryChange(category)}
                >
                  {category}
                </Badge>
              ))}
              {categories.length > 10 && (
                <Badge
                  variant="outline"
                  className="cursor-pointer hover:bg-muted"
                  onClick={() => setShowAllCategories(!showAllCategories)}
                >
                  {showAllCategories ? `Show less (${categories.length - 10} hidden)` : `Show more (+${categories.length - 10})`}
                </Badge>
              )}
            </div>
          </div>
        )}

        {/* Selected Foods Counter */}
        {selectedFoods.length > 0 && (
          <div className="mb-6 p-4 bg-card rounded-2xl border border-border">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Heart className="w-5 h-5 text-success fill-current" />
                <span className="font-medium">
                  {selectedFoods.length} food{selectedFoods.length !== 1 ? 's' : ''} selected
                </span>
              </div>
              <Button onClick={() => setSelectedFoods([])} variant="outline" size="sm">
                Clear All
              </Button>
            </div>
          </div>
        )}

        {/* Search Results Info */}
        {searchTerm.length > 2 && (
          <div className="mb-4 p-3 bg-secondary/10 rounded-lg border border-secondary/20">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 text-sm">
                <Search className="w-4 h-4 text-muted-foreground" />
                {searchLoading ? (
                  <span className="text-muted-foreground">Searching for "{searchTerm}"...</span>
                ) : (
                  <span className="text-muted-foreground">
                    Found <strong className="text-foreground">{allFilteredFoods.length}</strong> foods matching <strong className="text-foreground">"{searchTerm}"</strong>
                  </span>
                )}
              </div>
              {totalPages > 1 && (
                <div className="text-sm text-muted-foreground">
                  Page {currentPage} of {totalPages} • Showing {displayFoods.length} of {allFilteredFoods.length}
                </div>
              )}
            </div>
          </div>
        )}

        {/* Browse Results Info */}
        {searchTerm.length <= 2 && allFilteredFoods.length > 0 && totalPages > 1 && (
          <div className="mb-4 p-3 bg-secondary/10 rounded-lg border border-secondary/20">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 text-sm">
                <Database className="w-4 h-4 text-muted-foreground" />
                <span className="text-muted-foreground">
                  Browsing {allFilteredFoods.length} foods
                  {selectedCategory !== 'all' && ` in "${selectedCategory}"`}
                </span>
              </div>
              <div className="text-sm text-muted-foreground">
                Page {currentPage} of {totalPages} • Showing {displayFoods.length} of {allFilteredFoods.length}
              </div>
            </div>
          </div>
        )}

        {/* Food Grid */}
        {isLoading ? (
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4 mb-8 pb-24">
            {[...Array(20)].map((_, i) => (
              <div key={i} className="space-y-3">
                <Skeleton className="aspect-square rounded-3xl" />
                <Skeleton className="h-4 w-3/4 mx-auto" />
                <div className="flex gap-1 justify-center">
                  <Skeleton className="h-6 w-12 rounded-full" />
                  <Skeleton className="h-6 w-16 rounded-full" />
                </div>
              </div>
            ))}
          </div>
        ) : displayFoods.length > 0 ? (
          <>
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4 mb-8 pb-24">
              {displayFoods.map((food, index) => (
                <FoodCard
                  key={`${food.foodNumber}-${food.language}`}
                  name={formatFoodName(food)}
                  image={getFoodImageUrl(food)}
                  tags={getFoodTags(food)}
                  isSelected={selectedFoods.includes(food.foodNumber)}
                  onClick={() => toggleFood(food.foodNumber)}
                  onError={(e) => {
                    // Fallback to local images if backend images fail
                    e.currentTarget.src = getFallbackImage(index);
                  }}
                />
              ))}
            </div>

            {/* Pagination Controls */}
            {totalPages > 1 && (
              <div className="flex items-center justify-center gap-2 mb-8">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={goToPreviousPage}
                  disabled={currentPage === 1}
                  className="flex items-center gap-2"
                >
                  <ArrowLeft className="w-4 h-4" />
                  Previous
                </Button>
                
                {/* Page numbers */}
                <div className="flex items-center gap-1">
                  {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                    let pageNum;
                    if (totalPages <= 5) {
                      pageNum = i + 1;
                    } else if (currentPage <= 3) {
                      pageNum = i + 1;
                    } else if (currentPage >= totalPages - 2) {
                      pageNum = totalPages - 4 + i;
                    } else {
                      pageNum = currentPage - 2 + i;
                    }
                    
                    return (
                      <Button
                        key={pageNum}
                        variant={currentPage === pageNum ? "default" : "outline"}
                        size="sm"
                        onClick={() => goToPage(pageNum)}
                        className="w-8 h-8 p-0"
                      >
                        {pageNum}
                      </Button>
                    );
                  })}
                </div>

                <Button
                  variant="outline"
                  size="sm"
                  onClick={goToNextPage}
                  disabled={currentPage === totalPages}
                  className="flex items-center gap-2"
                >
                  Next
                  <ArrowLeft className="w-4 h-4 rotate-180" />
                </Button>
              </div>
            )}
          </>
        ) : searchTerm.length > 2 ? (
          <div className="text-center py-12 pb-24">
            <div className="text-muted-foreground text-lg mb-4">
              No foods found matching "{searchTerm}"
            </div>
            <Button onClick={() => setSearchTerm('')} variant="outline">
              Clear Search
            </Button>
          </div>
        ) : (
          <div className="text-center py-12 pb-24">
            <div className="max-w-md mx-auto">
              <Search className="w-16 h-16 text-muted-foreground/50 mx-auto mb-4" />
              <div className="text-muted-foreground text-lg mb-4">
                Start typing to search through {allFoods.length.toLocaleString()} foods
              </div>
              <div className="text-sm text-muted-foreground mb-6">
                Or use the category filters above to browse by type
              </div>
              <div className="grid grid-cols-2 gap-2 text-sm text-muted-foreground">
                <div>• Try searching for "apple"</div>
                <div>• Try searching for "chicken"</div>
                <div>• Try searching for "pasta"</div>
                <div>• Try searching for "milk"</div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Floating Action Button */}
      {selectedFoods.length > 0 && (
        <div className="fixed bottom-6 left-6 right-6 z-50">
          <div className="max-w-md mx-auto">
            <Button
              onClick={handleGetSuggestions}
              className="w-full font-semibold py-4 text-lg transform hover:scale-105 transition-transform shadow-xl bg-gradient-to-r from-primary to-primary/80 hover:from-primary/90 hover:to-primary/70"
              size="lg"
            >
              <Sparkles className="w-5 h-5 mr-2" />
              Get AI Suggestions ({selectedFoods.length} selected)
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
