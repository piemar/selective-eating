package com.example.app.controllers;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controller for serving food images.
 * This approach bypasses static resource handler issues.
 */
@RestController
@RequestMapping("/image")
public class ImageController {

    @GetMapping("/foods/{filename:.+}")
    public ResponseEntity<Resource> getFoodImage(@PathVariable String filename) {
        try {
            // Get the working directory and construct the image path
            String workingDir = System.getProperty("user.dir");
            Path imagePath = Paths.get(workingDir, "data", "image", "foods", filename);
            File imageFile = imagePath.toFile();
            
            System.out.println("🖼️ Image request for: " + filename);
            System.out.println("   Looking at: " + imagePath.toAbsolutePath());
            System.out.println("   File exists: " + imageFile.exists());
            
            if (!imageFile.exists() || !imageFile.canRead()) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(imageFile);
            
            // Determine content type based on file extension
            MediaType mediaType = MediaType.IMAGE_JPEG; // Default
            if (filename.toLowerCase().endsWith(".png")) {
                mediaType = MediaType.IMAGE_PNG;
            } else if (filename.toLowerCase().endsWith(".gif")) {
                mediaType = MediaType.IMAGE_GIF;
            }
            
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(resource);
                    
        } catch (Exception e) {
            System.err.println("❌ Error serving image " + filename + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
