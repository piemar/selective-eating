package com.example.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Configuration for serving static resources like food images.
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Get the absolute path to the images directory
        String workingDir = System.getProperty("user.dir");
        File imageDir = new File(workingDir, "data/image/");
        String imageLocation = "file:" + imageDir.getAbsolutePath() + "/";
        
        System.out.println("📸 CONFIGURING STATIC RESOURCES:");
        System.out.println("   Working directory: " + workingDir);
        System.out.println("   Image directory: " + imageDir.getAbsolutePath());
        System.out.println("   Directory exists: " + imageDir.exists());
        System.out.println("   Resource location: " + imageLocation);
        System.out.println("   Files in directory: " + (imageDir.exists() ? imageDir.list().length : "N/A"));
        
        // Map /image/** to the data/image/ directory
        registry.addResourceHandler("/image/**")
                .addResourceLocations(imageLocation)
                .setCachePeriod(3600) // Cache for 1 hour
                .resourceChain(false);
        
        System.out.println("✅ STATIC RESOURCE HANDLERS CONFIGURED");
        System.out.println("   Mapped: /image/** -> " + imageLocation);
    }
}
