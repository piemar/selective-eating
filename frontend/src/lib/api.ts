// API configuration and utilities for backend communication

const API_BASE_URL = 'http://localhost:8083/api/api/v1';

// Configuration for mock mode
const USE_MOCK_DATA = import.meta.env.VITE_USE_MOCK_DATA === 'true' || false;

// Console indicator for developers
if (USE_MOCK_DATA) {
  console.log('🎭 Mock Mode Active - Using simulated data instead of backend API');
  console.log('   To disable: Set VITE_USE_MOCK_DATA=false in .env.local');
} else {
  console.log('🌐 Live Mode Active - Using real backend API calls');
  console.log('   To enable mock mode: Set VITE_USE_MOCK_DATA=true in .env.local');
}

// Import mock data
import { 
  mockFoods, 
  mockCategories, 
  mockSuggestions,
  mockUserExperiences,
  mockUserStats,
  getMockFoodsByLanguage,
  getMockFoodsByCategory,
  getMockFoodsBySearch,
  getMockFoodByNumber,
  simulateApiDelay
} from './mockData';

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
  getFoodCount: async (): Promise<number> => {
    if (USE_MOCK_DATA) {
      await simulateApiDelay();
      return mockFoods.length;
    }
    return fetchApi<number>('/foods/count');
  },
  
  // Get all food categories
  getCategories: async (): Promise<string[]> => {
    if (USE_MOCK_DATA) {
      await simulateApiDelay(300);
      return mockCategories;
    }
    return fetchApi<string[]>('/foods/categories');
  },
  
  // Get foods by language
  getFoodsByLanguage: async (language: 'en' | 'sv'): Promise<Food[]> => {
    if (USE_MOCK_DATA) {
      await simulateApiDelay(800);
      return getMockFoodsByLanguage(language);
    }
    return fetchApi<Food[]>(`/foods/language/${language}`);
  },
  
  // Search foods by name
  searchFoods: async (searchTerm: string): Promise<Food[]> => {
    if (USE_MOCK_DATA) {
      await simulateApiDelay(400);
      return getMockFoodsBySearch(searchTerm);
    }
    return fetchApi<Food[]>(`/foods/search?name=${encodeURIComponent(searchTerm)}`);
  },
  
  // Get specific food by number
  getFoodByNumber: async (foodNumber: number): Promise<Food> => {
    if (USE_MOCK_DATA) {
      await simulateApiDelay(200);
      const food = getMockFoodByNumber(foodNumber);
      if (!food) {
        throw new Error(`Food with number ${foodNumber} not found`);
      }
      return food;
    }
    return fetchApi<Food>(`/foods/${foodNumber}`);
  },
  
  // Get foods by category
  getFoodsByCategory: async (category: string): Promise<Food[]> => {
    if (USE_MOCK_DATA) {
      await simulateApiDelay(600);
      return getMockFoodsByCategory(category);
    }
    return fetchApi<Food[]>(`/foods/category/${encodeURIComponent(category)}`);
  },
  
  // Get animal-based foods
  getAnimalFoods: async (): Promise<Food[]> => {
    if (USE_MOCK_DATA) {
      await simulateApiDelay(500);
      return mockFoods.filter(food => 
        food.foodCategory.toLowerCase().includes('meat') ||
        food.foodCategory.toLowerCase().includes('fish') ||
        food.foodCategory.toLowerCase().includes('poultry') ||
        food.foodCategory.toLowerCase().includes('dairy') ||
        food.foodCategory.toLowerCase().includes('milk') ||
        food.foodCategory.toLowerCase().includes('cheese')
      );
    }
    return fetchApi<Food[]>('/foods/animal');
  },
  
  // Get plant-based foods  
  getPlantFoods: async (): Promise<Food[]> => {
    if (USE_MOCK_DATA) {
      await simulateApiDelay(500);
      return mockFoods.filter(food => 
        food.foodCategory.toLowerCase().includes('fruit') ||
        food.foodCategory.toLowerCase().includes('vegetable') ||
        food.foodCategory.toLowerCase().includes('cereal') ||
        food.foodCategory.toLowerCase().includes('pasta') ||
        food.foodCategory.toLowerCase().includes('rice') ||
        food.foodCategory.toLowerCase().includes('bread')
      );
    }
    return fetchApi<Food[]>('/foods/plant');
  },
  
  // Get popular food suggestions
  getPopularSuggestions: async (maxSuggestions: number = 6): Promise<FoodSuggestion[]> => {
    if (USE_MOCK_DATA) {
      await simulateApiDelay(600);
      return mockSuggestions.slice(0, maxSuggestions);
    }
    return fetchApi<FoodSuggestion[]>(`/suggestions/popular?maxSuggestions=${maxSuggestions}`);
  },
  
  // Get personalized suggestions based on liked foods
  getPersonalizedSuggestions: async (likedFoodNumbers: number[], maxSuggestions: number = 5): Promise<FoodSuggestion[]> => {
    if (USE_MOCK_DATA) {
      await simulateApiDelay(800);
      // Filter suggestions based on liked foods for more realistic mock data
      const relevantSuggestions = mockSuggestions.filter(suggestion =>
        suggestion.basedOnFoods?.some(foodNum => likedFoodNumbers.includes(foodNum))
      );
      return relevantSuggestions.length > 0 
        ? relevantSuggestions.slice(0, maxSuggestions)
        : mockSuggestions.slice(0, maxSuggestions);
    }
    return fetchApi<FoodSuggestion[]>('/suggestions', {
      method: 'POST',
      body: JSON.stringify({ likedFoodNumbers, maxSuggestions }),
    });
  },
  
  // Log a food experience
  logFoodExperience: async (experience: LogFoodExperienceRequest): Promise<FoodExperience> => {
    if (USE_MOCK_DATA) {
      await simulateApiDelay(400);
      // Create a mock experience response
      const mockExperience: FoodExperience = {
        id: `mock-${Date.now()}`,
        userId: experience.userId,
        foodNumber: experience.foodNumber,
        foodName: experience.foodName,
        rating: experience.rating,
        notes: experience.notes || '',
        context: experience.context || '',
        childAge: '3-5', // Mock child age
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      return mockExperience;
    }
    return fetchApi<FoodExperience>('/experiences', {
      method: 'POST',
      body: JSON.stringify(experience),
    });
  },
  
  // Get user statistics
  getUserStats: async (userId: string): Promise<UserStats> => {
    if (USE_MOCK_DATA) {
      await simulateApiDelay(400);
      return mockUserStats;
    }
    return fetchApi<UserStats>(`/experiences/user/${userId}/stats`);
  },
  
  // Get user's food experiences
  getUserExperiences: async (userId: string): Promise<FoodExperience[]> => {
    if (USE_MOCK_DATA) {
      await simulateApiDelay(500);
      return mockUserExperiences;
    }
    return fetchApi<FoodExperience[]>(`/experiences/user/${userId}`);
  },
  
  // Get user's liked food numbers
  getUserLikedFoods: async (userId: string): Promise<number[]> => {
    if (USE_MOCK_DATA) {
      await simulateApiDelay(300);
      return mockUserExperiences
        .filter(exp => exp.rating >= 4)
        .map(exp => exp.foodNumber);
    }
    return fetchApi<number[]>(`/experiences/user/${userId}/liked-foods`);
  },
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
