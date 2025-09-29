package com.example.app.integration;

import com.example.app.BaseIntegrationTest;
import com.example.app.models.CommunityPost;
import com.example.app.models.Food;
import com.example.app.models.User;
import com.example.app.repositories.CommunityPostRepository;
import com.example.app.repositories.FoodRepository;
import com.example.app.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@AutoConfigureWebMvc
class CommunityControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CommunityPostRepository communityPostRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FoodRepository foodRepository;

    @LocalServerPort
    private int port;

    private User testAuthor1;
    private User testAuthor2;
    private Food testFood1;
    private Food testFood2;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/v1/community";
    }

    @BeforeEach
    void setUpTestData() {
        // Create test authors
        testAuthor1 = new User("author1@example.com", "Author One", "password123", Arrays.asList("USER"));
        testAuthor1 = userRepository.save(testAuthor1);

        testAuthor2 = new User("author2@example.com", "Author Two", "password123", Arrays.asList("USER"));
        testAuthor2 = userRepository.save(testAuthor2);

        // Create test foods
        testFood1 = new Food("Apple", "Red apple", "apple.jpg", Arrays.asList("Sweet", "Crunchy"));
        testFood1.setCategories(Arrays.asList("Fruit"));
        testFood1 = foodRepository.save(testFood1);

        testFood2 = new Food("Broccoli", "Green vegetable", "broccoli.jpg", Arrays.asList("Healthy", "Green"));
        testFood2.setCategories(Arrays.asList("Vegetable"));
        testFood2 = foodRepository.save(testFood2);
    }

    @Test
    @DisplayName("Should create a new community post and save to database")
    void shouldCreateCommunityPostAndSaveToDatabase() {
        // Given
        CommunityPost newPost = createSamplePost(testAuthor1, "My child finally tried vegetables!", 
            "We had a breakthrough with broccoli today!");
        newPost.setPostType("celebration");
        newPost.setTags(Arrays.asList("success-story", "vegetables"));
        
        // When
        ResponseEntity<CommunityPost> response = restTemplate.postForEntity(
            getBaseUrl() + "/posts", newPost, CommunityPost.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("My child finally tried vegetables!");
        assertThat(response.getBody().getContent()).isEqualTo("We had a breakthrough with broccoli today!");
        assertThat(response.getBody().getPostType()).isEqualTo("celebration");
        assertThat(response.getBody().getId()).isNotNull();
        
        // Verify post is saved in database
        List<CommunityPost> savedPosts = communityPostRepository.findByAuthor(testAuthor1);
        assertThat(savedPosts).hasSize(1);
        assertThat(savedPosts.get(0).getTitle()).isEqualTo("My child finally tried vegetables!");
        assertThat(savedPosts.get(0).getTags()).containsExactlyInAnyOrder("success-story", "vegetables");
    }

    @Test
    @DisplayName("Should create a food-related community post")
    void shouldCreateFoodRelatedCommunityPost() {
        // Given
        CommunityPost foodPost = new CommunityPost(testAuthor1, "Apple was a hit!", testFood1, 5);
        foodPost.setTitle("First time with apples");
        foodPost.setPostType("experience");
        foodPost.setTags(Arrays.asList("first-time", "success"));
        
        // When
        ResponseEntity<CommunityPost> response = restTemplate.postForEntity(
            getBaseUrl() + "/posts", foodPost, CommunityPost.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEqualTo("Apple was a hit!");
        assertThat(response.getBody().getFoodRating()).isEqualTo(5);
        
        // Verify in database with food relationship
        Optional<CommunityPost> savedPost = communityPostRepository.findById(response.getBody().getId());
        assertThat(savedPost).isPresent();
        assertThat(savedPost.get().getRelatedFood()).isNotNull();
        assertThat(savedPost.get().getRelatedFood().getName()).isEqualTo("Apple");
    }

    @Test
    @DisplayName("Should retrieve community post by ID from database")
    void shouldRetrieveCommunityPostByIdFromDatabase() {
        // Given
        CommunityPost post = createSamplePost(testAuthor2, "Looking for advice", "Any tips for picky eaters?");
        post.setPostType("question");
        CommunityPost savedPost = communityPostRepository.save(post);
        
        // When
        ResponseEntity<CommunityPost> response = restTemplate.getForEntity(
            getBaseUrl() + "/posts/" + savedPost.getId(), CommunityPost.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(savedPost.getId());
        assertThat(response.getBody().getTitle()).isEqualTo("Looking for advice");
        assertThat(response.getBody().getContent()).isEqualTo("Any tips for picky eaters?");
        assertThat(response.getBody().getPostType()).isEqualTo("question");
    }

    @Test
    @DisplayName("Should return 404 when community post not found")
    void shouldReturn404WhenCommunityPostNotFound() {
        // When
        ResponseEntity<CommunityPost> response = restTemplate.getForEntity(
            getBaseUrl() + "/posts/nonexistent-id", CommunityPost.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Should update existing community post in database")
    void shouldUpdateExistingCommunityPostInDatabase() {
        // Given
        CommunityPost post = createSamplePost(testAuthor1, "Original Title", "Original content");
        post.setPostType("tip");
        post.setLikesCount(5);
        CommunityPost savedPost = communityPostRepository.save(post);
        
        CommunityPost updatedPost = createSamplePost(testAuthor1, "Updated Title", "Updated content with more details");
        updatedPost.setPostType("tip");
        updatedPost.setTags(Arrays.asList("helpful", "updated"));
        updatedPost.setLikesCount(10);
        updatedPost.setCommentsCount(3);
        
        HttpEntity<CommunityPost> requestEntity = new HttpEntity<>(updatedPost);
        
        // When
        ResponseEntity<CommunityPost> response = restTemplate.exchange(
            getBaseUrl() + "/posts/" + savedPost.getId(), 
            HttpMethod.PUT, 
            requestEntity, 
            CommunityPost.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(savedPost.getId());
        assertThat(response.getBody().getTitle()).isEqualTo("Updated Title");
        assertThat(response.getBody().getContent()).isEqualTo("Updated content with more details");
        
        // Verify update in database
        Optional<CommunityPost> dbPost = communityPostRepository.findById(savedPost.getId());
        assertThat(dbPost).isPresent();
        assertThat(dbPost.get().getTitle()).isEqualTo("Updated Title");
        assertThat(dbPost.get().getTags()).containsExactlyInAnyOrder("helpful", "updated");
        assertThat(dbPost.get().getLikesCount()).isEqualTo(10);
        assertThat(dbPost.get().getCommentsCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should delete community post from database")
    void shouldDeleteCommunityPostFromDatabase() {
        // Given
        CommunityPost post = createSamplePost(testAuthor1, "Delete Me", "This post will be deleted");
        CommunityPost savedPost = communityPostRepository.save(post);
        
        // When
        ResponseEntity<Void> response = restTemplate.exchange(
            getBaseUrl() + "/posts/" + savedPost.getId(), 
            HttpMethod.DELETE, 
            null, 
            Void.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        
        // Verify deletion from database
        Optional<CommunityPost> deletedPost = communityPostRepository.findById(savedPost.getId());
        assertThat(deletedPost).isEmpty();
    }

    @Test
    @DisplayName("Should retrieve all community posts with pagination from database")
    void shouldRetrieveAllCommunityPostsWithPaginationFromDatabase() {
        // Given
        List<CommunityPost> posts = Arrays.asList(
            createSamplePost(testAuthor1, "Post 1", "Content 1"),
            createSamplePost(testAuthor2, "Post 2", "Content 2"),
            createSamplePost(testAuthor1, "Post 3", "Content 3")
        );
        communityPostRepository.saveAll(posts);
        
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
            getBaseUrl() + "/posts?page=0&size=2", String.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).contains("\"totalElements\":3");
        assertThat(response.getBody()).contains("\"size\":2");
        assertThat(response.getBody()).contains("\"number\":0");
    }

    @Test
    @DisplayName("Should test community post queries by author")
    void shouldTestCommunityPostQueriesByAuthor() {
        // Given
        List<CommunityPost> posts = Arrays.asList(
            createSamplePost(testAuthor1, "Author1 Post1", "Content1"),
            createSamplePost(testAuthor1, "Author1 Post2", "Content2"),
            createSamplePost(testAuthor2, "Author2 Post1", "Content3"),
            createSamplePost(testAuthor2, "Author2 Post2", "Content4")
        );
        communityPostRepository.saveAll(posts);
        
        // When & Then - Test findByAuthor
        List<CommunityPost> author1Posts = communityPostRepository.findByAuthor(testAuthor1);
        assertThat(author1Posts).hasSize(2);
        assertThat(author1Posts).extracting("title")
            .containsExactlyInAnyOrder("Author1 Post1", "Author1 Post2");
        
        List<CommunityPost> author2Posts = communityPostRepository.findByAuthor(testAuthor2);
        assertThat(author2Posts).hasSize(2);
        assertThat(author2Posts).extracting("title")
            .containsExactlyInAnyOrder("Author2 Post1", "Author2 Post2");
    }

    @Test
    @DisplayName("Should test community post queries by type")
    void shouldTestCommunityPostQueriesByType() {
        // Given
        List<CommunityPost> posts = Arrays.asList(
            createTypedPost(testAuthor1, "Success Story", "We did it!", "celebration"),
            createTypedPost(testAuthor2, "Need Help", "Any advice?", "question"),
            createTypedPost(testAuthor1, "Great Tip", "Try this!", "tip"),
            createTypedPost(testAuthor2, "Another Success", "Worked great!", "celebration")
        );
        communityPostRepository.saveAll(posts);
        
        // When & Then - Test findByPostType
        List<CommunityPost> celebrationPosts = communityPostRepository.findByPostType("celebration");
        assertThat(celebrationPosts).hasSize(2);
        assertThat(celebrationPosts).extracting("title")
            .containsExactlyInAnyOrder("Success Story", "Another Success");
        
        List<CommunityPost> questionPosts = communityPostRepository.findByPostType("question");
        assertThat(questionPosts).hasSize(1);
        assertThat(questionPosts.get(0).getTitle()).isEqualTo("Need Help");
        
        List<CommunityPost> tipPosts = communityPostRepository.findByPostType("tip");
        assertThat(tipPosts).hasSize(1);
        assertThat(tipPosts.get(0).getTitle()).isEqualTo("Great Tip");
    }

    @Test
    @DisplayName("Should test community post queries by related food")
    void shouldTestCommunityPostQueriesByRelatedFood() {
        // Given
        CommunityPost applePost1 = new CommunityPost(testAuthor1, "Love apples!", testFood1, 5);
        applePost1.setTitle("Apple Success");
        
        CommunityPost applePost2 = new CommunityPost(testAuthor2, "Apples work!", testFood1, 4);
        applePost2.setTitle("Another Apple Win");
        
        CommunityPost broccoliPost = new CommunityPost(testAuthor1, "Broccoli breakthrough", testFood2, 3);
        broccoliPost.setTitle("Vegetable Victory");
        
        communityPostRepository.saveAll(Arrays.asList(applePost1, applePost2, broccoliPost));
        
        // When & Then - Test findByRelatedFood
        List<CommunityPost> applePosts = communityPostRepository.findByRelatedFood(testFood1);
        assertThat(applePosts).hasSize(2);
        assertThat(applePosts).extracting("title")
            .containsExactlyInAnyOrder("Apple Success", "Another Apple Win");
        
        List<CommunityPost> broccoliPosts = communityPostRepository.findByRelatedFood(testFood2);
        assertThat(broccoliPosts).hasSize(1);
        assertThat(broccoliPosts.get(0).getTitle()).isEqualTo("Vegetable Victory");
        
        // Test findByRelatedFoodId
        List<CommunityPost> applePostsById = communityPostRepository.findByRelatedFoodId(testFood1.getId());
        assertThat(applePostsById).hasSize(2);
    }

    @Test
    @DisplayName("Should test community post queries by tags")
    void shouldTestCommunityPostQueriesByTags() {
        // Given
        CommunityPost post1 = createSamplePost(testAuthor1, "Success Story", "Great success!");
        post1.setTags(Arrays.asList("success-story", "milestone"));
        
        CommunityPost post2 = createSamplePost(testAuthor2, "Need Advice", "Help please");
        post2.setTags(Arrays.asList("question", "help"));
        
        CommunityPost post3 = createSamplePost(testAuthor1, "Another Success", "More success!");
        post3.setTags(Arrays.asList("success-story", "celebration"));
        
        CommunityPost post4 = createSamplePost(testAuthor2, "Milestone Reached", "Big achievement!");
        post4.setTags(Arrays.asList("milestone", "celebration"));
        
        communityPostRepository.saveAll(Arrays.asList(post1, post2, post3, post4));
        
        // When & Then - Test findByTagsContaining
        List<CommunityPost> successPosts = communityPostRepository.findByTagsContaining("success-story");
        assertThat(successPosts).hasSize(2);
        assertThat(successPosts).extracting("title")
            .containsExactlyInAnyOrder("Success Story", "Another Success");
        
        List<CommunityPost> milestonePosts = communityPostRepository.findByTagsContaining("milestone");
        assertThat(milestonePosts).hasSize(2);
        assertThat(milestonePosts).extracting("title")
            .containsExactlyInAnyOrder("Success Story", "Milestone Reached");
        
        List<CommunityPost> celebrationPosts = communityPostRepository.findByTagsContaining("celebration");
        assertThat(celebrationPosts).hasSize(2);
        assertThat(celebrationPosts).extracting("title")
            .containsExactlyInAnyOrder("Another Success", "Milestone Reached");
        
        // Test findByTagsIn
        List<CommunityPost> multipleTags = communityPostRepository.findByTagsIn(Arrays.asList("success-story", "help"));
        assertThat(multipleTags).hasSizeGreaterThanOrEqualTo(3); // Posts with either tag
    }

    @Test
    @DisplayName("Should test community post moderation queries")
    void shouldTestCommunityPostModerationQueries() {
        // Given
        CommunityPost approvedPost = createSamplePost(testAuthor1, "Approved Post", "Good content");
        approvedPost.setIsApproved(true);
        approvedPost.setIsFlagged(false);
        
        CommunityPost unapprovedPost = createSamplePost(testAuthor2, "Pending Post", "Awaiting approval");
        unapprovedPost.setIsApproved(false);
        unapprovedPost.setIsFlagged(false);
        
        CommunityPost flaggedPost = createSamplePost(testAuthor1, "Flagged Post", "Inappropriate content");
        flaggedPost.setIsApproved(true);
        flaggedPost.setIsFlagged(true);
        flaggedPost.setModeratorNotes("Contains inappropriate language");
        
        communityPostRepository.saveAll(Arrays.asList(approvedPost, unapprovedPost, flaggedPost));
        
        // When & Then - Test moderation queries
        List<CommunityPost> approvedPosts = communityPostRepository.findByIsApproved(true);
        assertThat(approvedPosts).hasSize(2);
        assertThat(approvedPosts).extracting("title")
            .containsExactlyInAnyOrder("Approved Post", "Flagged Post");
        
        List<CommunityPost> unapprovedPosts = communityPostRepository.findByIsApproved(false);
        assertThat(unapprovedPosts).hasSize(1);
        assertThat(unapprovedPosts.get(0).getTitle()).isEqualTo("Pending Post");
        
        List<CommunityPost> needsModerationPosts = communityPostRepository.findByIsApprovedAndIsFlagged(false, false);
        assertThat(needsModerationPosts).hasSize(1);
        assertThat(needsModerationPosts.get(0).getTitle()).isEqualTo("Pending Post");
        
        List<CommunityPost> flaggedApprovedPosts = communityPostRepository.findByIsApprovedAndIsFlagged(true, true);
        assertThat(flaggedApprovedPosts).hasSize(1);
        assertThat(flaggedApprovedPosts.get(0).getTitle()).isEqualTo("Flagged Post");
    }

    @Test
    @DisplayName("Should test community post time-based queries")
    void shouldTestCommunityPostTimeBasedQueries() {
        // Given
        Instant twoDaysAgo = Instant.now().minus(2, ChronoUnit.DAYS);
        Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant now = Instant.now();
        
        CommunityPost oldPost = createSamplePost(testAuthor1, "Old Post", "Old content");
        oldPost.setCreatedAt(twoDaysAgo);
        
        CommunityPost recentPost = createSamplePost(testAuthor2, "Recent Post", "Recent content");
        recentPost.setCreatedAt(yesterday);
        
        CommunityPost newPost = createSamplePost(testAuthor1, "New Post", "New content");
        newPost.setCreatedAt(now);
        
        communityPostRepository.saveAll(Arrays.asList(oldPost, recentPost, newPost));
        
        // When & Then - Test time-based queries
        List<CommunityPost> recentPosts = communityPostRepository.findByCreatedAtAfter(
            yesterday.minus(1, ChronoUnit.HOURS));
        assertThat(recentPosts).hasSizeGreaterThanOrEqualTo(2);
        
        List<CommunityPost> allRecentPosts = communityPostRepository.findByCreatedAtAfter(twoDaysAgo.minus(1, ChronoUnit.HOURS));
        assertThat(allRecentPosts).hasSize(3);
        
        // Test date range queries
        List<CommunityPost> rangePosts = communityPostRepository.findByCreatedAtBetween(
            twoDaysAgo.minus(1, ChronoUnit.HOURS), yesterday.plus(1, ChronoUnit.HOURS));
        assertThat(rangePosts).hasSize(2);
        assertThat(rangePosts).extracting("title")
            .containsExactlyInAnyOrder("Old Post", "Recent Post");
    }

    @Test
    @DisplayName("Should test community post engagement metrics")
    void shouldTestCommunityPostEngagementMetrics() {
        // Given
        CommunityPost popularPost = createSamplePost(testAuthor1, "Popular Post", "Everyone loves this");
        popularPost.setLikesCount(50);
        popularPost.setCommentsCount(20);
        popularPost.setSharesCount(10);
        
        CommunityPost moderatePost = createSamplePost(testAuthor2, "Moderate Post", "Some engagement");
        moderatePost.setLikesCount(15);
        moderatePost.setCommentsCount(5);
        moderatePost.setSharesCount(2);
        
        CommunityPost newPost = createSamplePost(testAuthor1, "New Post", "Just posted");
        newPost.setLikesCount(0);
        newPost.setCommentsCount(0);
        newPost.setSharesCount(0);
        
        communityPostRepository.saveAll(Arrays.asList(popularPost, moderatePost, newPost));
        
        // When & Then - Verify engagement data integrity
        List<CommunityPost> allPosts = communityPostRepository.findAll();
        assertThat(allPosts).hasSize(3);
        
        CommunityPost retrievedPopularPost = allPosts.stream()
            .filter(post -> post.getTitle().equals("Popular Post"))
            .findFirst()
            .orElse(null);
        
        assertThat(retrievedPopularPost).isNotNull();
        assertThat(retrievedPopularPost.getLikesCount()).isEqualTo(50);
        assertThat(retrievedPopularPost.getCommentsCount()).isEqualTo(20);
        assertThat(retrievedPopularPost.getSharesCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should test community post data integrity with food relationships")
    void shouldTestCommunityPostDataIntegrityWithFoodRelationships() {
        // Given
        CommunityPost foodPost = new CommunityPost(testAuthor1, "Amazing experience with apples", testFood1, 5);
        foodPost.setTitle("Apple Success Story");
        foodPost.setPostType("experience");
        foodPost.setTags(Arrays.asList("success", "first-time", "fruit"));
        foodPost.setLikesCount(25);
        
        CommunityPost savedPost = communityPostRepository.save(foodPost);
        
        // When - Retrieve the post
        Optional<CommunityPost> retrievedPost = communityPostRepository.findById(savedPost.getId());
        
        // Then - Verify all fields and relationships are preserved
        assertThat(retrievedPost).isPresent();
        CommunityPost post = retrievedPost.get();
        
        assertThat(post.getAuthor().getId()).isEqualTo(testAuthor1.getId());
        assertThat(post.getAuthor().getName()).isEqualTo("Author One");
        assertThat(post.getTitle()).isEqualTo("Apple Success Story");
        assertThat(post.getContent()).isEqualTo("Amazing experience with apples");
        assertThat(post.getRelatedFood()).isNotNull();
        assertThat(post.getRelatedFood().getId()).isEqualTo(testFood1.getId());
        assertThat(post.getRelatedFood().getName()).isEqualTo("Apple");
        assertThat(post.getFoodRating()).isEqualTo(5);
        assertThat(post.getPostType()).isEqualTo("experience");
        assertThat(post.getTags()).containsExactlyInAnyOrder("success", "first-time", "fruit");
        assertThat(post.getLikesCount()).isEqualTo(25);
        assertThat(post.getIsApproved()).isTrue(); // Default value
        assertThat(post.getIsFlagged()).isFalse(); // Default value
        assertThat(post.getCreatedAt()).isNotNull();
        assertThat(post.getUpdatedAt()).isNotNull();
    }

    private CommunityPost createSamplePost(User author, String title, String content) {
        CommunityPost post = new CommunityPost(author, content);
        post.setTitle(title);
        return post;
    }

    private CommunityPost createTypedPost(User author, String title, String content, String postType) {
        CommunityPost post = createSamplePost(author, title, content);
        post.setPostType(postType);
        return post;
    }
}
