package com.example.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Alternative base test class for environments without Docker/TestContainers.
 * Uses the configured MongoDB instance instead of spinning up containers.
 * 
 * Usage: Extend this class instead of BaseIntegrationTest when Docker is not available.
 * Make sure MongoDB is running locally on mongodb://localhost:27017/selective_eating_test
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class SimpleIntegrationTestBase {

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Clear all collections before each test
        // Note: This assumes MongoDB is running locally
        try {
            mongoTemplate.getDb().drop();
        } catch (Exception e) {
            System.err.println("Warning: Could not clear test database. Make sure MongoDB is running on mongodb://localhost:27017/selective_eating_test");
            System.err.println("Error: " + e.getMessage());
        }
    }

    protected String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
