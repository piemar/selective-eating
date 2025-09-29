package com.example.app.config;

import com.example.app.models.Food;
import com.example.app.repositories.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private FoodRepository foodRepository;

    @Override
    public void run(String... args) throws Exception {
        // Only initialize if database is empty
        if (foodRepository.count() == 0) {
            initializeFoods();
        }
    }

    private void initializeFoods() {
        List<Food> foods = List.of(
            createFood("Banana", "Sweet and soft yellow fruit", "banana.png", 
                      List.of("Sweet", "Soft", "Yellow"),
                      List.of("Fruits"),
                      List.of("Soft", "Smooth"), List.of("Sweet", "Mild"), List.of("Yellow", "Curved")),
            
            createFood("Pasta", "Versatile wheat-based food", "pasta.png", 
                      List.of("Mild", "Chewy", "Warm"),
                      List.of("Grains"),
                      List.of("Chewy", "Smooth"), List.of("Mild", "Neutral"), List.of("Long", "White")),
            
            createFood("Apple", "Crunchy and sweet fruit", "apple.png", 
                      List.of("Crunchy", "Sweet", "Red"),
                      List.of("Fruits"),
                      List.of("Crunchy", "Firm"), List.of("Sweet", "Tart"), List.of("Red", "Round")),
            
            createFood("Carrot", "Orange root vegetable", "carrot.png", 
                      List.of("Crunchy", "Sweet", "Orange"),
                      List.of("Vegetables"),
                      List.of("Crunchy", "Firm"), List.of("Sweet", "Mild"), List.of("Orange", "Long")),
            
            createFood("Mango", "Tropical sweet fruit", "mango.png", 
                      List.of("Sweet", "Soft", "Orange"),
                      List.of("Fruits"),
                      List.of("Soft", "Smooth"), List.of("Sweet", "Tropical"), List.of("Orange", "Yellow")),
            
            createFood("Rice", "Staple grain food", "rice.png", 
                      List.of("Mild", "Soft", "Warm"),
                      List.of("Grains"),
                      List.of("Soft", "Smooth"), List.of("Mild", "Neutral"), List.of("White", "Small")),
            
            createFood("Pear", "Sweet and juicy fruit", "pear.png", 
                      List.of("Sweet", "Soft", "Light"),
                      List.of("Fruits"),
                      List.of("Soft", "Juicy"), List.of("Sweet", "Mild"), List.of("Green", "Yellow")),
            
            createFood("Sweet Potato", "Orange root vegetable", "sweet-potato.png", 
                      List.of("Sweet", "Orange", "Soft"),
                      List.of("Vegetables"),
                      List.of("Soft", "Smooth"), List.of("Sweet", "Mild"), List.of("Orange", "Oval"))
        );

        foodRepository.saveAll(foods);
        System.out.println("Initialized database with " + foods.size() + " foods");
    }

    private Food createFood(String name, String description, String imageUrl, List<String> tags,
                           List<String> categories, List<String> textureProperties, 
                           List<String> flavorProperties, List<String> visualProperties) {
        Food food = new Food(name, description, imageUrl, tags);
        food.setCategories(categories);
        food.setTextureProperties(textureProperties);
        food.setFlavorProperties(flavorProperties);
        food.setVisualProperties(visualProperties);
        food.setIsCommonAllergen(false); // Default to non-allergenic
        return food;
    }
}
