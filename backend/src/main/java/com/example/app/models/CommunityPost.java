package com.example.app.models;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;
import java.util.List;

@Document("community_posts")
public class CommunityPost {
    @Id
    private String id;

    @DBRef
    @Indexed
    private User author;

    private String content;
    private String title;
    
    // Related food information
    @DBRef
    private Food relatedFood;
    private Integer foodRating; // 1-5 stars if sharing a food experience
    
    // Engagement metrics
    private Integer likesCount = 0;
    private Integer commentsCount = 0;
    private Integer sharesCount = 0;
    
    // Post categorization
    private List<String> tags; // e.g., ["success-story", "tip", "question", "milestone"]
    private String postType; // "experience", "tip", "question", "celebration", "support"
    
    // Moderation
    private Boolean isApproved = true;
    private Boolean isFlagged = false;
    private String moderatorNotes;
    
    @CreatedDate
    @Indexed
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    // Default constructor
    public CommunityPost() {
    }

    // Constructor with essential fields
    public CommunityPost(User author, String content) {
        this.author = author;
        this.content = content;
    }

    // Constructor with food experience
    public CommunityPost(User author, String content, Food relatedFood, Integer foodRating) {
        this.author = author;
        this.content = content;
        this.relatedFood = relatedFood;
        this.foodRating = foodRating;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Food getRelatedFood() {
        return relatedFood;
    }

    public void setRelatedFood(Food relatedFood) {
        this.relatedFood = relatedFood;
    }

    public Integer getFoodRating() {
        return foodRating;
    }

    public void setFoodRating(Integer foodRating) {
        this.foodRating = foodRating;
    }

    public Integer getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(Integer likesCount) {
        this.likesCount = likesCount;
    }

    public Integer getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(Integer commentsCount) {
        this.commentsCount = commentsCount;
    }

    public Integer getSharesCount() {
        return sharesCount;
    }

    public void setSharesCount(Integer sharesCount) {
        this.sharesCount = sharesCount;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getPostType() {
        return postType;
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }

    public Boolean getIsApproved() {
        return isApproved;
    }

    public void setIsApproved(Boolean isApproved) {
        this.isApproved = isApproved;
    }

    public Boolean getIsFlagged() {
        return isFlagged;
    }

    public void setIsFlagged(Boolean isFlagged) {
        this.isFlagged = isFlagged;
    }

    public String getModeratorNotes() {
        return moderatorNotes;
    }

    public void setModeratorNotes(String moderatorNotes) {
        this.moderatorNotes = moderatorNotes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
