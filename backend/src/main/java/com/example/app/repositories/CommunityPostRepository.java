package com.example.app.repositories;

import com.example.app.models.CommunityPost;
import com.example.app.models.Food;
import com.example.app.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface CommunityPostRepository extends MongoRepository<CommunityPost, String> {
    
    // Find posts by author
    List<CommunityPost> findByAuthor(User author);
    Page<CommunityPost> findByAuthor(User author, Pageable pageable);
    
    // Find posts by type
    List<CommunityPost> findByPostType(String postType);
    Page<CommunityPost> findByPostType(String postType, Pageable pageable);
    
    // Find posts by related food
    List<CommunityPost> findByRelatedFood(Food food);
    List<CommunityPost> findByRelatedFoodId(String foodId);
    
    // Find posts by tags
    List<CommunityPost> findByTagsContaining(String tag);
    Page<CommunityPost> findByTagsContaining(String tag, Pageable pageable);
    
    // Find posts with multiple tags
    @Query("{'tags': {'$in': ?0}}")
    List<CommunityPost> findByTagsIn(List<String> tags);
    Page<CommunityPost> findByTagsIn(List<String> tags, Pageable pageable);
    
    // Find approved posts only
    List<CommunityPost> findByIsApproved(Boolean isApproved);
    Page<CommunityPost> findByIsApproved(Boolean isApproved, Pageable pageable);
    
    // Find posts that need moderation
    List<CommunityPost> findByIsApprovedAndIsFlagged(Boolean isApproved, Boolean isFlagged);
    
    // Find recent posts
    List<CommunityPost> findByCreatedAtAfter(Instant after);
    Page<CommunityPost> findByCreatedAtAfter(Instant after, Pageable pageable);
    
    // Find posts in date range
    List<CommunityPost> findByCreatedAtBetween(Instant start, Instant end);
    Page<CommunityPost> findByCreatedAtBetween(Instant start, Instant end, Pageable pageable);
    
    // Find popular posts (high engagement)
    @Query("{'likesCount': {'$gte': ?0}}")
    List<CommunityPost> findPopularPosts(Integer minLikes);
    
    @Query("{'likesCount': {'$gte': ?0}}")
    Page<CommunityPost> findPopularPosts(Integer minLikes, Pageable pageable);
    
    // Search posts by content
    @Query("{'$or': [" +
           "{'content': {'$regex': ?0, '$options': 'i'}}, " +
           "{'title': {'$regex': ?0, '$options': 'i'}}" +
           "]}")
    List<CommunityPost> searchByContentOrTitle(String searchTerm);
    
    @Query("{'$or': [" +
           "{'content': {'$regex': ?0, '$options': 'i'}}, " +
           "{'title': {'$regex': ?0, '$options': 'i'}}" +
           "]}")
    Page<CommunityPost> searchByContentOrTitle(String searchTerm, Pageable pageable);
    
    // Find posts with food ratings
    List<CommunityPost> findByFoodRatingIsNotNull();
    List<CommunityPost> findByFoodRating(Integer rating);
    
    // Find posts by food rating range
    @Query("{'foodRating': {'$gte': ?0, '$lte': ?1}}")
    List<CommunityPost> findByFoodRatingBetween(Integer minRating, Integer maxRating);
    
    // Get trending posts (recent posts with high engagement)
    @Query("{'createdAt': {'$gte': ?0}, 'likesCount': {'$gte': ?1}}")
    List<CommunityPost> findTrendingPosts(Instant since, Integer minLikes);
    
    @Query("{'createdAt': {'$gte': ?0}, 'likesCount': {'$gte': ?1}}")
    Page<CommunityPost> findTrendingPosts(Instant since, Integer minLikes, Pageable pageable);
    
    // Statistics queries
    Long countByAuthor(User author);
    Long countByPostType(String postType);
    Long countByIsApproved(Boolean isApproved);
    Long countByCreatedAtAfter(Instant after);
    
    // Find posts ordered by engagement
    @Query(value = "{}", sort = "{'likesCount': -1, 'commentsCount': -1}")
    Page<CommunityPost> findAllOrderByEngagement(Pageable pageable);
    
    // Find posts by author in date range
    List<CommunityPost> findByAuthorAndCreatedAtBetween(User author, Instant start, Instant end);
    
    // Count posts by author in timeframe
    Long countByAuthorAndCreatedAtAfter(User author, Instant after);
}
