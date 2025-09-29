package com.example.app.services;

import com.example.app.models.CommunityPost;
import com.example.app.models.Food;
import com.example.app.models.User;
import com.example.app.repositories.CommunityPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class CommunityService {

    private final CommunityPostRepository communityPostRepository;

    @Autowired
    public CommunityService(CommunityPostRepository communityPostRepository) {
        this.communityPostRepository = communityPostRepository;
    }

    // Create operations
    public CommunityPost createPost(CommunityPost post) {
        if (post.getAuthor() == null) {
            throw new RuntimeException("Community post must have an author");
        }
        if (post.getContent() == null || post.getContent().trim().isEmpty()) {
            throw new RuntimeException("Community post must have content");
        }

        // Initialize engagement metrics
        if (post.getLikesCount() == null) post.setLikesCount(0);
        if (post.getCommentsCount() == null) post.setCommentsCount(0);
        if (post.getSharesCount() == null) post.setSharesCount(0);

        // Set moderation defaults
        if (post.getIsApproved() == null) post.setIsApproved(true); // Auto-approve by default
        if (post.getIsFlagged() == null) post.setIsFlagged(false);

        return communityPostRepository.save(post);
    }

    // Read operations
    public Page<CommunityPost> getAllPosts(Pageable pageable) {
        return communityPostRepository.findByIsApproved(true, pageable);
    }

    public Page<CommunityPost> getAllPostsForModeration(Pageable pageable) {
        return communityPostRepository.findAll(pageable);
    }

    public Optional<CommunityPost> getPostById(String id) {
        return communityPostRepository.findById(id);
    }

    public Page<CommunityPost> getPostsByAuthor(User author, Pageable pageable) {
        return communityPostRepository.findByAuthor(author, pageable);
    }

    public Page<CommunityPost> getPostsByType(String postType, Pageable pageable) {
        return communityPostRepository.findByPostType(postType, pageable);
    }

    public List<CommunityPost> getPostsByFood(Food food) {
        return communityPostRepository.findByRelatedFood(food);
    }

    public Page<CommunityPost> getPostsByTag(String tag, Pageable pageable) {
        return communityPostRepository.findByTagsContaining(tag, pageable);
    }

    public Page<CommunityPost> getPostsByTags(List<String> tags, Pageable pageable) {
        return communityPostRepository.findByTagsIn(tags, pageable);
    }

    // Recent and trending posts
    public Page<CommunityPost> getRecentPosts(int days, Pageable pageable) {
        Instant since = Instant.now().minusSeconds(days * 24 * 60 * 60);
        return communityPostRepository.findByCreatedAtAfter(since, pageable);
    }

    public Page<CommunityPost> getTrendingPosts(int days, int minLikes, Pageable pageable) {
        Instant since = Instant.now().minusSeconds(days * 24 * 60 * 60);
        return communityPostRepository.findTrendingPosts(since, minLikes, pageable);
    }

    public Page<CommunityPost> getPopularPosts(int minLikes, Pageable pageable) {
        return communityPostRepository.findPopularPosts(minLikes, pageable);
    }

    public Page<CommunityPost> getMostEngagedPosts(Pageable pageable) {
        return communityPostRepository.findAllOrderByEngagement(pageable);
    }

    // Search operations
    public Page<CommunityPost> searchPosts(String searchTerm, Pageable pageable) {
        return communityPostRepository.searchByContentOrTitle(searchTerm, pageable);
    }

    // Engagement operations
    public CommunityPost likePost(String postId) {
        CommunityPost post = communityPostRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        
        post.setLikesCount(post.getLikesCount() + 1);
        return communityPostRepository.save(post);
    }

    public CommunityPost unlikePost(String postId) {
        CommunityPost post = communityPostRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        
        post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
        return communityPostRepository.save(post);
    }

    public CommunityPost incrementComments(String postId) {
        CommunityPost post = communityPostRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        
        post.setCommentsCount(post.getCommentsCount() + 1);
        return communityPostRepository.save(post);
    }

    public CommunityPost decrementComments(String postId) {
        CommunityPost post = communityPostRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        
        post.setCommentsCount(Math.max(0, post.getCommentsCount() - 1));
        return communityPostRepository.save(post);
    }

    public CommunityPost sharePost(String postId) {
        CommunityPost post = communityPostRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        
        post.setSharesCount(post.getSharesCount() + 1);
        return communityPostRepository.save(post);
    }

    // Moderation operations
    public CommunityPost approvePost(String postId) {
        CommunityPost post = communityPostRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        
        post.setIsApproved(true);
        post.setIsFlagged(false);
        return communityPostRepository.save(post);
    }

    public CommunityPost flagPost(String postId, String reason) {
        CommunityPost post = communityPostRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        
        post.setIsFlagged(true);
        post.setIsApproved(false);
        post.setModeratorNotes(reason);
        return communityPostRepository.save(post);
    }

    public List<CommunityPost> getPostsNeedingModeration() {
        return communityPostRepository.findByIsApprovedAndIsFlagged(false, true);
    }

    // Update operations
    public CommunityPost updatePost(String id, CommunityPost postDetails) {
        CommunityPost post = communityPostRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        // Update allowed fields
        if (postDetails.getContent() != null) {
            post.setContent(postDetails.getContent());
        }
        if (postDetails.getTitle() != null) {
            post.setTitle(postDetails.getTitle());
        }
        if (postDetails.getTags() != null) {
            post.setTags(postDetails.getTags());
        }
        if (postDetails.getPostType() != null) {
            post.setPostType(postDetails.getPostType());
        }
        if (postDetails.getFoodRating() != null) {
            post.setFoodRating(postDetails.getFoodRating());
        }

        return communityPostRepository.save(post);
    }

    // Delete operations
    public void deletePost(String id) {
        if (!communityPostRepository.existsById(id)) {
            throw new RuntimeException("Post not found with id: " + id);
        }
        communityPostRepository.deleteById(id);
    }

    // Statistics methods
    public long getTotalPostCount() {
        return communityPostRepository.count();
    }

    public long getPostCountByAuthor(User author) {
        return communityPostRepository.countByAuthor(author);
    }

    public long getPostCountByType(String postType) {
        return communityPostRepository.countByPostType(postType);
    }

    public long getRecentPostCount(int days) {
        Instant since = Instant.now().minusSeconds(days * 24 * 60 * 60);
        return communityPostRepository.countByCreatedAtAfter(since);
    }

    public long getApprovedPostCount() {
        return communityPostRepository.countByIsApproved(true);
    }

    // User activity statistics
    public long getUserPostCountInTimeframe(User author, int days) {
        Instant since = Instant.now().minusSeconds(days * 24 * 60 * 60);
        return communityPostRepository.countByAuthorAndCreatedAtAfter(author, since);
    }

    public List<CommunityPost> getUserPostsInRange(User author, Instant start, Instant end) {
        return communityPostRepository.findByAuthorAndCreatedAtBetween(author, start, end);
    }

    // Content filtering
    public Page<CommunityPost> getPostsWithFoodRatings(Pageable pageable) {
        return communityPostRepository.findAll(pageable); // Would need to add this query to repository
    }

    public List<CommunityPost> getPostsByFoodRating(Integer rating) {
        return communityPostRepository.findByFoodRating(rating);
    }

    public List<CommunityPost> getPostsByFoodRatingRange(Integer minRating, Integer maxRating) {
        return communityPostRepository.findByFoodRatingBetween(minRating, maxRating);
    }

    // Helper methods
    public CommunityPost savePost(CommunityPost post) {
        return communityPostRepository.save(post);
    }
}
