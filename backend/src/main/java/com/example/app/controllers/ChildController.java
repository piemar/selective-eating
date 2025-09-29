package com.example.app.controllers;

import com.example.app.models.Child;
import com.example.app.models.User;
import com.example.app.services.ChildService;
import com.example.app.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/children")
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173"})
public class ChildController {

    private final ChildService childService;
    private final UserService userService;

    @Autowired
    public ChildController(ChildService childService, UserService userService) {
        this.childService = childService;
        this.userService = userService;
    }

    // Basic CRUD operations
    @GetMapping
    public List<Child> getAllChildren() {
        return childService.getAllChildren();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Child> getChildById(@PathVariable String id) {
        Optional<Child> child = childService.getChildById(id);
        return child.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Child> createChild(@RequestBody Child child) {
        try {
            Child createdChild = childService.createChild(child);
            return ResponseEntity.ok(createdChild);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Child> updateChild(@PathVariable String id, @RequestBody Child childDetails) {
        try {
            Child updatedChild = childService.updateChild(id, childDetails);
            return ResponseEntity.ok(updatedChild);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChild(@PathVariable String id) {
        try {
            childService.deleteChild(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Parent-based operations
    @GetMapping("/parent/{parentId}")
    public List<Child> getChildrenByParent(@PathVariable String parentId) {
        return childService.getChildrenByParentId(parentId);
    }

    @GetMapping("/by-parent-email/{email}")
    public ResponseEntity<List<Child>> getChildrenByParentEmail(@PathVariable String email) {
        Optional<User> parent = userService.getUserByEmail(email);
        if (parent.isPresent()) {
            List<Child> children = childService.getChildrenByParent(parent.get());
            return ResponseEntity.ok(children);
        }
        return ResponseEntity.notFound().build();
    }

    // Progress tracking operations
    @PatchMapping("/{id}/foods-tried")
    public ResponseEntity<Child> updateFoodsTried(@PathVariable String id, @RequestParam int increment) {
        try {
            Child updatedChild = childService.updateFoodsTried(id, increment);
            return ResponseEntity.ok(updatedChild);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/favorites")
    public ResponseEntity<Child> updateFavoritesCount(@PathVariable String id, @RequestParam int increment) {
        try {
            Child updatedChild = childService.updateFavoritesCount(id, increment);
            return ResponseEntity.ok(updatedChild);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/streak")
    public ResponseEntity<Child> updateStreak(@PathVariable String id, @RequestParam int streak) {
        try {
            Child updatedChild = childService.updateStreak(id, streak);
            return ResponseEntity.ok(updatedChild);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/progress")
    public ResponseEntity<Child> updateExplorationProgress(@PathVariable String id, @RequestParam double progress) {
        try {
            Child updatedChild = childService.updateExplorationProgress(id, progress);
            return ResponseEntity.ok(updatedChild);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Food preference operations
    @PostMapping("/{id}/liked-foods/{foodId}")
    public ResponseEntity<Child> addLikedFood(@PathVariable String id, @PathVariable String foodId) {
        try {
            Child updatedChild = childService.addLikedFood(id, foodId);
            return ResponseEntity.ok(updatedChild);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/disliked-foods/{foodId}")
    public ResponseEntity<Child> addDislikedFood(@PathVariable String id, @PathVariable String foodId) {
        try {
            Child updatedChild = childService.addDislikedFood(id, foodId);
            return ResponseEntity.ok(updatedChild);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}/food-preferences/{foodId}")
    public ResponseEntity<Child> removeFoodPreference(@PathVariable String id, @PathVariable String foodId) {
        try {
            Child updatedChild = childService.removeFoodPreference(id, foodId);
            return ResponseEntity.ok(updatedChild);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Query operations
    @GetMapping("/dietary-restriction/{restriction}")
    public List<Child> getChildrenWithDietaryRestriction(@PathVariable String restriction) {
        return childService.getChildrenWithDietaryRestriction(restriction);
    }

    @GetMapping("/allergen/{allergen}")
    public List<Child> getChildrenWithAllergen(@PathVariable String allergen) {
        return childService.getChildrenWithAllergen(allergen);
    }

    @GetMapping("/active")
    public List<Child> getActiveChildren() {
        return childService.getActiveChildren();
    }

    @GetMapping("/high-progress")
    public List<Child> getHighProgressChildren(@RequestParam(defaultValue = "70.0") double minProgress) {
        return childService.getHighProgressChildren(minProgress);
    }

    // Statistics
    @GetMapping("/count")
    public Long getChildCount() {
        return childService.getChildCount();
    }

    @GetMapping("/count/parent/{parentId}")
    public ResponseEntity<Long> getChildCountByParent(@PathVariable String parentId) {
        Optional<User> parent = userService.getUserById(parentId);
        if (parent.isPresent()) {
            Long count = childService.getChildCountByParent(parent.get());
            return ResponseEntity.ok(count);
        }
        return ResponseEntity.notFound().build();
    }
}
