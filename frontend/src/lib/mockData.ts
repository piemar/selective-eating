// Mock data for development and testing
import { Food, FoodSuggestion } from './api';

// Mock foods data
export const mockFoods: Food[] = [
  {
    id: '1',
    foodNumber: 66,
    name: 'Whey cheese app. 30% fat',
    altName: 'Whey cheese',
    language: 'en',
    foodCategory: 'Whey products',
    imageUrl: 'image/foods/66_Whey_cheese_app.jpg'
  },
  {
    id: '2', 
    foodNumber: 70,
    name: 'Cottage cheese plain 4% fat',
    altName: 'Cottage cheese',
    language: 'en',
    foodCategory: 'Fresh cheese and quark',
    imageUrl: 'image/foods/70_Bos_taurus.jpg'
  },
  {
    id: '3',
    foodNumber: 71,
    name: 'Cottage cheese w/ fruit 3% fat',
    altName: 'Cottage cheese with fruit',
    language: 'en',
    foodCategory: 'Fresh cheese and quark',
    imageUrl: 'image/foods/71_Cottage_cheese_fruit.jpg'
  },
  {
    id: '4',
    foodNumber: 72,
    name: 'Cottage cheese w/ vegetables 3.5-5% fat',
    altName: 'Cottage cheese with vegetables',
    language: 'en',
    foodCategory: 'Fresh cheese and quark',
    imageUrl: 'image/foods/72_Cottage_cheese.jpg'
  },
  {
    id: '5',
    foodNumber: 588,
    name: 'Apple w/ skin',
    altName: 'Apple with skin',
    language: 'en',
    foodCategory: 'Fruit fresh',
    imageUrl: 'image/foods/588_Apple.jpg'
  },
  {
    id: '6',
    foodNumber: 589,
    name: 'Apple w/o peel',
    altName: 'Apple without peel',
    language: 'en',
    foodCategory: 'Fruit fresh',
    imageUrl: 'image/foods/589_Apple.jpg'
  },
  {
    id: '7',
    foodNumber: 550,
    name: 'Pineapple',
    language: 'en',
    foodCategory: 'Fruit fresh',
    imageUrl: 'image/foods/550_Pineapple.jpg'
  },
  {
    id: '8',
    foodNumber: 590,
    name: 'Banana',
    language: 'en', 
    foodCategory: 'Fruit fresh',
    imageUrl: 'image/foods/590_Banana.jpg'
  },
  {
    id: '9',
    foodNumber: 1200,
    name: 'Chicken breast grilled',
    altName: 'Grilled chicken breast',
    language: 'en',
    foodCategory: 'Meat poultry',
    imageUrl: 'image/foods/1200_Chicken.jpg'
  },
  {
    id: '10',
    foodNumber: 1300,
    name: 'Pasta cooked',
    altName: 'Cooked pasta',
    language: 'en',
    foodCategory: 'Cereals pasta',
    imageUrl: 'image/foods/1300_Pasta.jpg'
  },
  {
    id: '11',
    foodNumber: 1400,
    name: 'Rice white cooked',
    altName: 'Cooked white rice',
    language: 'en',
    foodCategory: 'Cereals rice',
    imageUrl: 'image/foods/1400_Rice.jpg'
  },
  {
    id: '12',
    foodNumber: 1500,
    name: 'Bread white sliced',
    altName: 'White bread',
    language: 'en',
    foodCategory: 'Bread and rolls',
    imageUrl: 'image/foods/1500_Bread.jpg'
  },
  {
    id: '13',
    foodNumber: 113,
    name: 'Human breastmilk',
    altName: 'Breast milk',
    language: 'en',
    foodCategory: 'Milk and milk products',
    imageUrl: 'image/foods/113_Milk.jpg'
  },
  {
    id: '14',
    foodNumber: 1600,
    name: 'Milk whole 3.5% fat',
    altName: 'Whole milk',
    language: 'en',
    foodCategory: 'Milk and milk products', 
    imageUrl: 'image/foods/1600_Milk.jpg'
  },
  {
    id: '15',
    foodNumber: 1700,
    name: 'Yogurt plain',
    altName: 'Plain yogurt',
    language: 'en',
    foodCategory: 'Milk and milk products',
    imageUrl: 'image/foods/1700_Yogurt.jpg'
  },
  // Add some Swedish foods for language testing
  {
    id: '16',
    foodNumber: 66,
    name: 'Vassleost app. 30% fett',
    altName: 'Vassleost',
    language: 'sv',
    foodCategory: 'Vassleprodukter',
    imageUrl: 'image/foods/66_Whey_cheese_app.jpg'
  },
  {
    id: '17',
    foodNumber: 588,
    name: 'Äpple med skal',
    altName: 'Äpple',
    language: 'sv',
    foodCategory: 'Frukt färsk',
    imageUrl: 'image/foods/588_Apple.jpg'
  }
];

// Mock categories
export const mockCategories: string[] = [
  'Whey products',
  'Fresh cheese and quark',
  'Fruit fresh',
  'Meat poultry',
  'Cereals pasta',
  'Cereals rice',
  'Bread and rolls',
  'Milk and milk products',
  'Vegetables fresh',
  'Fish and seafood',
  'Other fats (lard, tallow, coconut oil)',
  'Mixed origin fat',
  'Artificial sweetener',
  'Sauces and dressings'
];

// Mock suggestions
export const mockSuggestions: FoodSuggestion[] = [
  {
    id: null,
    foodNumber: 71,
    foodName: 'Cottage cheese w/ fruit 3% fat',
    imageUrl: 'image/foods/71_Cottage_cheese_fruit.jpg',
    tags: ['Kid-friendly', 'Dairy'],
    reason: 'Great choice because it has same food category, similar texture and taste',
    confidenceScore: 0.85,
    basedOnFoods: [66, 70, 72],
    createdAt: new Date().toISOString()
  },
  {
    id: null,
    foodNumber: 590,
    foodName: 'Banana',
    imageUrl: 'image/foods/590_Banana.jpg',
    tags: ['Kid-friendly', 'Sweet'],
    reason: 'Popular choice for children - naturally sweet and soft texture',
    confidenceScore: 0.9,
    basedOnFoods: [588, 589],
    createdAt: new Date().toISOString()
  },
  {
    id: null,
    foodNumber: 1300,
    foodName: 'Pasta cooked',
    imageUrl: 'image/foods/1300_Pasta.jpg',
    tags: ['Kid-friendly', 'Comfort food'],
    reason: 'Familiar texture and mild flavor - great for expanding food preferences',
    confidenceScore: 0.8,
    basedOnFoods: [1400, 1500],
    createdAt: new Date().toISOString()
  },
  {
    id: null,
    foodNumber: 1600,
    foodName: 'Milk whole 3.5% fat',
    imageUrl: 'image/foods/1600_Milk.jpg',
    tags: ['Kid-friendly', 'Nutrition'],
    reason: 'Excellent source of calcium and protein - similar creamy texture to liked foods',
    confidenceScore: 0.75,
    basedOnFoods: [66, 70, 71],
    createdAt: new Date().toISOString()
  }
];

// Helper functions for mock data
export const getMockFoodsByLanguage = (language: 'en' | 'sv'): Food[] => {
  return mockFoods.filter(food => food.language === language);
};

export const getMockFoodsByCategory = (category: string): Food[] => {
  return mockFoods.filter(food => food.foodCategory === category);
};

export const getMockFoodsBySearch = (searchTerm: string): Food[] => {
  const term = searchTerm.toLowerCase();
  return mockFoods.filter(food => 
    food.name.toLowerCase().includes(term) ||
    food.altName?.toLowerCase().includes(term) ||
    food.foodCategory.toLowerCase().includes(term)
  );
};

export const getMockFoodByNumber = (foodNumber: number): Food | undefined => {
  return mockFoods.find(food => food.foodNumber === foodNumber);
};

// Mock user experiences
export const mockUserExperiences = [
  {
    id: '1',
    userId: 'mock-user',
    foodNumber: 66,
    foodName: 'Whey cheese app. 30% fat',
    rating: 4,
    notes: 'Child enjoyed the mild flavor',
    context: 'snack',
    childAge: '3-5',
    createdAt: '2024-01-15T10:30:00Z',
    updatedAt: '2024-01-15T10:30:00Z'
  },
  {
    id: '2',
    userId: 'mock-user',
    foodNumber: 588,
    foodName: 'Apple w/ skin',
    rating: 5,
    notes: 'Loves the crunch and sweetness',
    context: 'snack',
    childAge: '3-5',
    createdAt: '2024-01-16T15:45:00Z',
    updatedAt: '2024-01-16T15:45:00Z'
  }
];

// Mock user stats
export const mockUserStats = {
  totalFoodsTried: 12,
  positiveFoods: 8,
  positivePercentage: 67,
  streak: 3,
  recentAchievements: [
    'Tried 5 new fruits this week!',
    'First time enjoying dairy products',
    'Completed a full meal with vegetables'
  ]
};

// Simulate API delay for realistic testing
export const simulateApiDelay = (ms: number = 500): Promise<void> => {
  return new Promise(resolve => setTimeout(resolve, ms));
};
