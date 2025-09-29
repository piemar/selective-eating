// API configuration and utilities for backend communication

const API_BASE_URL = 'http://localhost:8083/api/api/v1';  

// Food types matching backend models
export interface Food {
  id: string;
  foodNumber: number;
  name: string;
  altName?: string;
  scientificName?: string;
  language: 'en' | 'sv';
  foodCategory: string;
  imageUrl?: string;
}

export interface FoodSearchResult {
  foods: Food[];
  total: number;
}

export interface FoodSuggestion {
  foodNumber: number;
  foodName: string;
  imageUrl?: string;
  tags: string[];
  reason: string;
  confidenceScore: number;
  basedOnFoods: number[];
  createdAt?: string;
}

export interface FoodExperience {
  id: string;
  userId: string;
  foodNumber: number;
  foodName: string;
  rating: number;
  notes?: string;
  context?: string;
  childAge?: string;
  createdAt: string;
  updatedAt: string;
}

export interface LogFoodExperienceRequest {
  userId: string;
  foodNumber: number;
  foodName: string;
  rating: number;
  notes?: string;
  context?: string;
}

export interface UserStats {
  totalFoodsTried: number;
  positiveFoods: number;
  positivePercentage: number;
  streak: number;
  recentAchievements: string[];
}

class ApiError extends Error {
  constructor(message: string, public status: number) {
    super(message);
    this.name = 'ApiError';
  }
}

// Generic fetch wrapper with error handling
async function fetchApi<T>(endpoint: string, options?: RequestInit): Promise<T> {
  const defaultOptions: RequestInit = {
    headers: {
      'Content-Type': 'application/json',
    },
  };
  
  const finalOptions = { ...defaultOptions, ...options };
  const response = await fetch(`${API_BASE_URL}${endpoint}`, finalOptions);
  
  if (!response.ok) {
    throw new ApiError(
      `API request failed: ${response.statusText}`,
      response.status
    );
  }
  
  return response.json();
}

// API functions
export const api = {
  // Get total food count
  getFoodCount: () => fetchApi<number>('/foods/count'),
  
  // Get all food categories
  getCategories: () => fetchApi<string[]>('/foods/categories'),
  
  // Get foods by language
  getFoodsByLanguage: (language: 'en' | 'sv') => 
    fetchApi<Food[]>(`/foods/language/${language}`),
  
  // Search foods by name
  searchFoods: (searchTerm: string) => 
    fetchApi<Food[]>(`/foods/search?name=${encodeURIComponent(searchTerm)}`),
  
  // Get specific food by number
  getFoodByNumber: (foodNumber: number) => 
    fetchApi<Food>(`/foods/${foodNumber}`),
  
  // Get foods by category
  getFoodsByCategory: (category: string) => 
    fetchApi<Food[]>(`/foods/category/${encodeURIComponent(category)}`),
  
  // Get animal-based foods
  getAnimalFoods: () => fetchApi<Food[]>('/foods/animal'),
  
  // Get plant-based foods  
  getPlantFoods: () => fetchApi<Food[]>('/foods/plant'),
  
  // Get popular food suggestions
  getPopularSuggestions: (maxSuggestions: number = 6) => 
    fetchApi<FoodSuggestion[]>(`/suggestions/popular?maxSuggestions=${maxSuggestions}`),
  
  // Get personalized suggestions based on liked foods
  getPersonalizedSuggestions: (likedFoodNumbers: number[], maxSuggestions: number = 5) =>
    fetchApi<FoodSuggestion[]>('/suggestions', {
      method: 'POST',
      body: JSON.stringify({ likedFoodNumbers, maxSuggestions }),
    }),
  
  // Log a food experience
  logFoodExperience: (experience: LogFoodExperienceRequest) =>
    fetchApi<FoodExperience>('/experiences', {
      method: 'POST',
      body: JSON.stringify(experience),
    }),
  
  // Get user statistics
  getUserStats: (userId: string) =>
    fetchApi<UserStats>(`/experiences/user/${userId}/stats`),
  
  // Get user's food experiences
  getUserExperiences: (userId: string) =>
    fetchApi<FoodExperience[]>(`/experiences/user/${userId}`),
  
  // Get user's liked food numbers
  getUserLikedFoods: (userId: string) =>
    fetchApi<number[]>(`/experiences/user/${userId}/liked-foods`),
};

// Helper function to get food image URL
export const getFoodImageUrl = (food: Food): string => {
  if (food.imageUrl) {
    // Images are served by the backend ImageController (port 8083)
    // The imageUrl field contains the path like "image/foods/1_Bos_taurus.jpg"
    // Backend has context-path: /api, so we need to prefix with /api
    return `http://localhost:8083/api/${food.imageUrl}`;
  }
  // Fallback to generate a consistent placeholder based on food number
  return `https://via.placeholder.com/64/f3f4f6/9ca3af?text=${encodeURIComponent(food.name.charAt(0).toUpperCase())}`;
};

// Helper function to format food name for display
export const formatFoodName = (food: Food): string => {
  return food.altName || food.name;
};

// Utility functions for user session management
export const getUserId = (): string => {
  let userId = localStorage.getItem('userId');
  if (!userId) {
    userId = `user_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    localStorage.setItem('userId', userId);
  }
  return userId;
};

// Helper function to get suggestion image URL
export const getSuggestionImageUrl = (suggestion: FoodSuggestion): string => {
  if (suggestion.imageUrl) {
    return `http://localhost:8083/api/${suggestion.imageUrl}`;
  }
  return `https://via.placeholder.com/64/f3f4f6/9ca3af?text=${encodeURIComponent(suggestion.foodName.charAt(0).toUpperCase())}`;
};

// Helper function to create food tags from category
export const getFoodTags = (food: Food): string[] => {
  const tags: string[] = [];
  
  if (food.foodCategory) {
    tags.push(food.foodCategory);
  }
  
  // Add language indicator
  tags.push(food.language === 'en' ? 'English' : 'Svenska');
  
  return tags;
};
