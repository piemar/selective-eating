package com.example.app.repository;

import com.example.app.BaseIntegrationTest;
import com.example.app.models.*;
import com.example.app.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class AllRepositoriesIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private FoodExperienceRepository foodExperienceRepository;

    @Autowired
    private CommunityPostRepository communityPostRepository;

    private User testUser1, testUser2;
    private Child testChild1, testChild2;
    private Food testFood1, testFood2, testFood3;

    @BeforeEach
    void setUpTestData() {
        // Create test users
        testUser1 = userRepository.save(new User("user1@test.com", "User One", "pass1", Arrays.asList("USER")));
        testUser2 = userRepository.save(new User("user2@test.com", "User Two", "pass2", Arrays.asList("USER", "ADMIN")));

        // Create test foods
        testFood1 = new Food("Apple", "Red apple", "apple.jpg", Arrays.asList("Sweet", "Crunchy"));
        testFood1.setCategories(Arrays.asList("Fruit"));
        testFood1.setTextureProperties(Arrays.asList("Crunchy", "Firm"));
        testFood1.setFlavorProperties(Arrays.asList("Sweet", "Fresh"));
        testFood1.setVisualProperties(Arrays.asList("Red", "Round"));
        testFood1.setAllergens(Arrays.asList());
        testFood1 = foodRepository.save(testFood1);

        testFood2 = new Food("Peanut", "Nut with protein", "peanut.jpg", Arrays.asList("Protein", "Crunchy"));
        testFood2.setCategories(Arrays.asList("Nuts"));
        testFood2.setTextureProperties(Arrays.asList("Crunchy", "Hard"));
        testFood2.setFlavorProperties(Arrays.asList("Nutty", "Rich"));
        testFood2.setVisualProperties(Arrays.asList("Brown", "Small"));
        testFood2.setAllergens(Arrays.asList("Nuts"));
        testFood2.setIsCommonAllergen(true);
        testFood2 = foodRepository.save(testFood2);

        testFood3 = new Food("Broccoli", "Green vegetable", "broccoli.jpg", Arrays.asList("Healthy", "Green"));
        testFood3.setCategories(Arrays.asList("Vegetable"));
        testFood3.setTextureProperties(Arrays.asList("Crunchy", "Fibrous"));
        testFood3.setFlavorProperties(Arrays.asList("Mild", "Earthy"));
        testFood3.setVisualProperties(Arrays.asList("Green", "Tree-like"));
        testFood3.setAllergens(Arrays.asList());
        testFood3 = foodRepository.save(testFood3);

        // Create test children
        testChild1 = new Child("Child One", LocalDate.of(2018, 5, 15), testUser1);
        testChild1.setTotalFoodsTried(10);
        testChild1.setCurrentStreak(3);
        testChild1.setExplorationProgress(60.0);
        testChild1.setPreferredTextures(Arrays.asList("Soft", "Smooth"));
        testChild1.setPreferredFlavors(Arrays.asList("Sweet", "Mild"));
        testChild1.setDietaryRestrictions(Arrays.asList("vegetarian"));
        testChild1.setAllergens(Arrays.asList("Nuts"));
        testChild1.setLikedFoodIds(Arrays.asList(testFood1.getId()));
        testChild1.setDislikedFoodIds(Arrays.asList(testFood2.getId()));
        testChild1 = childRepository.save(testChild1);

        testChild2 = new Child("Child Two", LocalDate.of(2019, 8, 20), testUser2);
        testChild2.setTotalFoodsTried(5);
        testChild2.setCurrentStreak(0);
        testChild2.setExplorationProgress(25.0);
        testChild2.setPreferredTextures(Arrays.asList("Crunchy"));
        testChild2.setPreferredFlavors(Arrays.asList("Savory"));
        testChild2.setDietaryRestrictions(Arrays.asList());
        testChild2.setAllergens(Arrays.asList());
        testChild2.setLikedFoodIds(Arrays.asList(testFood2.getId()));
        testChild2.setDislikedFoodIds(Arrays.asList(testFood3.getId()));
        testChild2 = childRepository.save(testChild2);
    }

    @Nested
    @DisplayName("User Repository Tests")
    class UserRepositoryTests {

        @Test
        @DisplayName("Should find user by email")
        void shouldFindUserByEmail() {
            // When
            Optional<User> found = userRepository.findByEmail("user1@test.com");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("User One");
            assertThat(found.get().getRoles()).containsExactly("USER");
        }

        @Test
        @DisplayName("Should check if email exists")
        void shouldCheckIfEmailExists() {
            // When & Then
            assertThat(userRepository.existsByEmail("user1@test.com")).isTrue();
            assertThat(userRepository.existsByEmail("nonexistent@test.com")).isFalse();
        }

        @Test
        @DisplayName("Should enforce unique email constraint")
        void shouldEnforceUniqueEmailConstraint() {
            // Given
            User duplicateUser = new User("user1@test.com", "Duplicate User", "pass", Arrays.asList("USER"));

            // When & Then
            assertThatThrownBy(() -> userRepository.save(duplicateUser))
                .isInstanceOf(Exception.class); // MongoDB will throw due to unique index
        }

        @Test
        @DisplayName("Should handle CRUD operations correctly")
        void shouldHandleCrudOperationsCorrectly() {
            // Create
            User newUser = new User("new@test.com", "New User", "pass", Arrays.asList("MODERATOR"));
            User saved = userRepository.save(newUser);
            assertThat(saved.getId()).isNotNull();

            // Read
            Optional<User> found = userRepository.findById(saved.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("New User");

            // Update
            found.get().setName("Updated User");
            User updated = userRepository.save(found.get());
            assertThat(updated.getName()).isEqualTo("Updated User");

            // Delete
            userRepository.deleteById(saved.getId());
            assertThat(userRepository.findById(saved.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Child Repository Tests")
    class ChildRepositoryTests {

        @Test
        @DisplayName("Should find children by parent")
        void shouldFindChildrenByParent() {
            // When
            List<Child> user1Children = childRepository.findByParent(testUser1);
            List<Child> user2Children = childRepository.findByParent(testUser2);
            List<Child> user1ChildrenById = childRepository.findByParentId(testUser1.getId());

            // Then
            assertThat(user1Children).hasSize(1);
            assertThat(user1Children.get(0).getName()).isEqualTo("Child One");
            
            assertThat(user2Children).hasSize(1);
            assertThat(user2Children.get(0).getName()).isEqualTo("Child Two");
            
            assertThat(user1ChildrenById).hasSize(1);
            assertThat(user1ChildrenById.get(0).getId()).isEqualTo(testChild1.getId());
        }

        @Test
        @DisplayName("Should find child by name and parent")
        void shouldFindChildByNameAndParent() {
            // When
            Optional<Child> found = childRepository.findByNameAndParent("Child One", testUser1);
            Optional<Child> notFound = childRepository.findByNameAndParent("Child One", testUser2);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(testChild1.getId());
            assertThat(notFound).isEmpty();
        }

        @Test
        @DisplayName("Should count children by parent")
        void shouldCountChildrenByParent() {
            // When
            Long count1 = childRepository.countByParent(testUser1);
            Long count2 = childRepository.countByParent(testUser2);

            // Then
            assertThat(count1).isEqualTo(1);
            assertThat(count2).isEqualTo(1);
        }

        @Test
        @DisplayName("Should find children by dietary restrictions")
        void shouldFindChildrenByDietaryRestrictions() {
            // When
            List<Child> vegetarians = childRepository.findByDietaryRestrictionsContaining("vegetarian");

            // Then
            assertThat(vegetarians).hasSize(1);
            assertThat(vegetarians.get(0).getName()).isEqualTo("Child One");
        }

        @Test
        @DisplayName("Should find children by allergens")
        void shouldFindChildrenByAllergens() {
            // When
            List<Child> nutAllergic = childRepository.findByAllergensContaining("Nuts");

            // Then
            assertThat(nutAllergic).hasSize(1);
            assertThat(nutAllergic.get(0).getName()).isEqualTo("Child One");
        }

        @Test
        @DisplayName("Should find children by preferences")
        void shouldFindChildrenByPreferences() {
            // When
            List<Child> softPreference = childRepository.findByPreferredTexturesContaining("Soft");
            List<Child> crunchyPreference = childRepository.findByPreferredTexturesContaining("Crunchy");
            List<Child> sweetPreference = childRepository.findByPreferredFlavorsContaining("Sweet");

            // Then
            assertThat(softPreference).hasSize(1);
            assertThat(softPreference.get(0).getName()).isEqualTo("Child One");
            
            assertThat(crunchyPreference).hasSize(1);
            assertThat(crunchyPreference.get(0).getName()).isEqualTo("Child Two");
            
            assertThat(sweetPreference).hasSize(1);
            assertThat(sweetPreference.get(0).getName()).isEqualTo("Child One");
        }

        @Test
        @DisplayName("Should find active children")
        void shouldFindActiveChildren() {
            // When
            List<Child> activeChildren = childRepository.findActiveChildren();

            // Then
            assertThat(activeChildren).hasSize(1);
            assertThat(activeChildren.get(0).getName()).isEqualTo("Child One");
        }

        @Test
        @DisplayName("Should find children by progress metrics")
        void shouldFindChildrenByProgressMetrics() {
            // When
            List<Child> experienced = childRepository.findByTotalFoodsTriedGreaterThanEqual(10);
            List<Child> highProgress = childRepository.findByExplorationProgressGreaterThanEqual(50.0);

            // Then
            assertThat(experienced).hasSize(1);
            assertThat(experienced.get(0).getName()).isEqualTo("Child One");
            
            assertThat(highProgress).hasSize(1);
            assertThat(highProgress.get(0).getName()).isEqualTo("Child One");
        }

        @Test
        @DisplayName("Should find children by food preferences")
        void shouldFindChildrenByFoodPreferences() {
            // When
            List<Child> likesApple = childRepository.findByLikedFoodIdsContaining(testFood1.getId());
            List<Child> likesPeanut = childRepository.findByLikedFoodIdsContaining(testFood2.getId());
            List<Child> dislikesPeanut = childRepository.findByDislikedFoodIdsContaining(testFood2.getId());

            // Then
            assertThat(likesApple).hasSize(1);
            assertThat(likesApple.get(0).getName()).isEqualTo("Child One");
            
            assertThat(likesPeanut).hasSize(1);
            assertThat(likesPeanut.get(0).getName()).isEqualTo("Child Two");
            
            assertThat(dislikesPeanut).hasSize(1);
            assertThat(dislikesPeanut.get(0).getName()).isEqualTo("Child One");
        }
    }

    @Nested
    @DisplayName("Food Repository Tests")
    class FoodRepositoryTests {

        @Test
        @DisplayName("Should find food by name")
        void shouldFindFoodByName() {
            // When
            Optional<Food> found = foodRepository.findByName("Apple");
            List<Food> containing = foodRepository.findByNameContainingIgnoreCase("app");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getDescription()).isEqualTo("Red apple");
            
            assertThat(containing).hasSize(1);
            assertThat(containing.get(0).getName()).isEqualTo("Apple");
        }

        @Test
        @DisplayName("Should find foods by categories and tags")
        void shouldFindFoodsByCategoriesAndTags() {
            // When
            List<Food> fruits = foodRepository.findByCategoriesContaining("Fruit");
            List<Food> nuts = foodRepository.findByCategoriesContaining("Nuts");
            List<Food> sweetFoods = foodRepository.findByTagsContaining("Sweet");
            List<Food> crunchyFoods = foodRepository.findByTagsContaining("Crunchy");

            // Then
            assertThat(fruits).hasSize(1);
            assertThat(fruits.get(0).getName()).isEqualTo("Apple");
            
            assertThat(nuts).hasSize(1);
            assertThat(nuts.get(0).getName()).isEqualTo("Peanut");
            
            assertThat(sweetFoods).hasSize(1);
            assertThat(sweetFoods.get(0).getName()).isEqualTo("Apple");
            
            assertThat(crunchyFoods).hasSize(3); // Apple, Peanut, Broccoli
        }

        @Test
        @DisplayName("Should find foods by texture properties")
        void shouldFindFoodsByTextureProperties() {
            // When
            List<Food> crunchyTexture = foodRepository.findByTexturePropertiesContaining("Crunchy");
            List<Food> firmTexture = foodRepository.findByTexturePropertiesContaining("Firm");

            // Then
            assertThat(crunchyTexture).hasSize(3); // All test foods are crunchy
            assertThat(firmTexture).hasSize(1);
            assertThat(firmTexture.get(0).getName()).isEqualTo("Apple");
        }

        @Test
        @DisplayName("Should find foods by flavor properties")
        void shouldFindFoodsByFlavorProperties() {
            // When
            List<Food> sweetFlavor = foodRepository.findByFlavorPropertiesContaining("Sweet");
            List<Food> mildFlavor = foodRepository.findByFlavorPropertiesContaining("Mild");

            // Then
            assertThat(sweetFlavor).hasSize(1);
            assertThat(sweetFlavor.get(0).getName()).isEqualTo("Apple");
            
            assertThat(mildFlavor).hasSize(1);
            assertThat(mildFlavor.get(0).getName()).isEqualTo("Broccoli");
        }

        @Test
        @DisplayName("Should find foods by visual properties")
        void shouldFindFoodsByVisualProperties() {
            // When
            List<Food> roundFoods = foodRepository.findByVisualPropertiesContaining("Round");
            List<Food> greenFoods = foodRepository.findByVisualPropertiesContaining("Green");

            // Then
            assertThat(roundFoods).hasSize(1);
            assertThat(roundFoods.get(0).getName()).isEqualTo("Apple");
            
            assertThat(greenFoods).hasSize(1);
            assertThat(greenFoods.get(0).getName()).isEqualTo("Broccoli");
        }

        @Test
        @DisplayName("Should filter foods by allergens")
        void shouldFilterFoodsByAllergens() {
            // When
            List<Food> safeForNutAllergy = foodRepository.findFoodsWithoutAllergens(Arrays.asList("Nuts"));
            List<Food> safeForMultipleAllergies = foodRepository.findFoodsWithoutAllergens(Arrays.asList("Nuts", "Dairy"));

            // Then
            assertThat(safeForNutAllergy).hasSize(2); // Apple and Broccoli
            assertThat(safeForNutAllergy).extracting("name")
                .containsExactlyInAnyOrder("Apple", "Broccoli");
            
            assertThat(safeForMultipleAllergies).hasSize(2); // Apple and Broccoli
        }

        @Test
        @DisplayName("Should find similar foods")
        void shouldFindSimilarFoods() {
            // When
            List<Food> similarToApple = foodRepository.findSimilarFoods(
                Arrays.asList("Crunchy", "Firm"),
                Arrays.asList("Sweet", "Fresh"),
                Arrays.asList("Red", "Round")
            );

            // Then
            assertThat(similarToApple).isNotEmpty();
            // The query should find foods with any of the specified properties
        }

        @Test
        @DisplayName("Should find foods by multiple tags")
        void shouldFindFoodsByMultipleTags() {
            // When
            List<Food> multipleTags = foodRepository.findByTagsIn(Arrays.asList("Sweet", "Healthy"));

            // Then
            assertThat(multipleTags).hasSize(2); // Apple (Sweet) and Broccoli (Healthy)
            assertThat(multipleTags).extracting("name")
                .containsExactlyInAnyOrder("Apple", "Broccoli");
        }
    }

    @Nested
    @DisplayName("Food Experience Repository Tests")
    class FoodExperienceRepositoryTests {

        private FoodExperience experience1, experience2, experience3;

        @BeforeEach
        void setUpExperiences() {
            experience1 = new FoodExperience(testChild1, testFood1, 5, "Loved it!");
            experience1.setReaction("liked");
            experience1.setWasFirstTime(true);
            experience1.setMealType("snack");
            experience1.setEnvironment("home");
            experience1.setCreatedAt(Instant.now().minus(1, ChronoUnit.DAYS));
            experience1 = foodExperienceRepository.save(experience1);

            experience2 = new FoodExperience(testChild1, testFood3, 2, "Not good");
            experience2.setReaction("disliked");
            experience2.setWasFirstTime(true);
            experience2.setMealType("dinner");
            experience2.setEnvironment("home");
            experience2.setCreatedAt(Instant.now().minus(2, ChronoUnit.HOURS));
            experience2 = foodExperienceRepository.save(experience2);

            experience3 = new FoodExperience(testChild2, testFood2, 4, "Pretty good");
            experience3.setReaction("liked");
            experience3.setWasFirstTime(false);
            experience3.setMealType("lunch");
            experience3.setEnvironment("school");
            experience3.setCreatedAt(Instant.now());
            experience3 = foodExperienceRepository.save(experience3);
        }

        @Test
        @DisplayName("Should find experiences by child")
        void shouldFindExperiencesByChild() {
            // When
            List<FoodExperience> child1Experiences = foodExperienceRepository.findByChild(testChild1);
            List<FoodExperience> child2Experiences = foodExperienceRepository.findByChild(testChild2);
            List<FoodExperience> child1ById = foodExperienceRepository.findByChildId(testChild1.getId());

            // Then
            assertThat(child1Experiences).hasSize(2);
            assertThat(child2Experiences).hasSize(1);
            assertThat(child1ById).hasSize(2);
        }

        @Test
        @DisplayName("Should find experiences by food")
        void shouldFindExperiencesByFood() {
            // When
            List<FoodExperience> appleExperiences = foodExperienceRepository.findByFood(testFood1);
            List<FoodExperience> peanutExperiences = foodExperienceRepository.findByFood(testFood2);
            List<FoodExperience> broccoliExperiences = foodExperienceRepository.findByFood(testFood3);

            // Then
            assertThat(appleExperiences).hasSize(1);
            assertThat(peanutExperiences).hasSize(1);
            assertThat(broccoliExperiences).hasSize(1);
        }

        @Test
        @DisplayName("Should find specific child-food experience")
        void shouldFindSpecificChildFoodExperience() {
            // When
            Optional<FoodExperience> child1Apple = foodExperienceRepository.findByChildAndFood(testChild1, testFood1);
            Optional<FoodExperience> child2Apple = foodExperienceRepository.findByChildAndFood(testChild2, testFood1);

            // Then
            assertThat(child1Apple).isPresent();
            assertThat(child1Apple.get().getRating()).isEqualTo(5);
            assertThat(child2Apple).isEmpty();
        }

        @Test
        @DisplayName("Should find experiences by rating")
        void shouldFindExperiencesByRating() {
            // When
            List<FoodExperience> rating5 = foodExperienceRepository.findByRating(5);
            List<FoodExperience> child1HighRated = foodExperienceRepository.findByChildAndRatingGreaterThanEqual(testChild1, 4);

            // Then
            assertThat(rating5).hasSize(1);
            assertThat(rating5.get(0).getNotes()).isEqualTo("Loved it!");
            
            assertThat(child1HighRated).hasSize(1);
            assertThat(child1HighRated.get(0).getRating()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should find experiences by reaction")
        void shouldFindExperiencesByReaction() {
            // When
            List<FoodExperience> liked = foodExperienceRepository.findByReaction("liked");
            List<FoodExperience> disliked = foodExperienceRepository.findByReaction("disliked");
            List<FoodExperience> child1Liked = foodExperienceRepository.findByChildAndReaction(testChild1, "liked");

            // Then
            assertThat(liked).hasSize(2); // experience1 and experience3
            assertThat(disliked).hasSize(1); // experience2
            assertThat(child1Liked).hasSize(1); // experience1
        }

        @Test
        @DisplayName("Should find experiences by time")
        void shouldFindExperiencesByTime() {
            // When
            Instant cutoff = Instant.now().minus(12, ChronoUnit.HOURS);
            List<FoodExperience> recent = foodExperienceRepository.findByCreatedAtAfter(cutoff);
            List<FoodExperience> child1Recent = foodExperienceRepository.findByChildAndCreatedAtAfter(testChild1, cutoff);

            // Then
            assertThat(recent).hasSize(2); // experience2 and experience3
            assertThat(child1Recent).hasSize(1); // experience2
        }

        @Test
        @DisplayName("Should find first-time experiences")
        void shouldFindFirstTimeExperiences() {
            // When
            List<FoodExperience> firstTime = foodExperienceRepository.findByChildAndWasFirstTime(testChild1, true);
            List<FoodExperience> notFirstTime = foodExperienceRepository.findByChildAndWasFirstTime(testChild2, false);

            // Then
            assertThat(firstTime).hasSize(2); // Both child1 experiences were first time
            assertThat(notFirstTime).hasSize(1); // Child2's peanut experience was not first time
        }

        @Test
        @DisplayName("Should find experiences by context")
        void shouldFindExperiencesByContext() {
            // When
            List<FoodExperience> child1HomeExperiences = foodExperienceRepository.findByChildAndEnvironment(testChild1, "home");
            List<FoodExperience> child2SchoolExperiences = foodExperienceRepository.findByChildAndEnvironment(testChild2, "school");
            List<FoodExperience> child1DinnerExperiences = foodExperienceRepository.findByChildAndMealType(testChild1, "dinner");

            // Then
            assertThat(child1HomeExperiences).hasSize(2); // experience1 and experience2
            assertThat(child2SchoolExperiences).hasSize(1); // experience3
            assertThat(child1DinnerExperiences).hasSize(1); // experience2
        }

        @Test
        @DisplayName("Should find experiences in date range")
        void shouldFindExperiencesInDateRange() {
            // When
            Instant start = Instant.now().minus(1, ChronoUnit.DAYS).minus(1, ChronoUnit.HOURS);
            Instant end = Instant.now().minus(1, ChronoUnit.HOURS);
            List<FoodExperience> rangeExperiences = foodExperienceRepository.findByChildAndCreatedAtBetween(testChild1, start, end);

            // Then
            assertThat(rangeExperiences).hasSize(1); // experience2 falls in this range
        }
    }

    @Nested
    @DisplayName("Community Post Repository Tests")
    class CommunityPostRepositoryTests {

        private CommunityPost post1, post2, post3, post4;

        @BeforeEach
        void setUpPosts() {
            post1 = new CommunityPost(testUser1, "Great success with apples!", testFood1, 5);
            post1.setTitle("Apple Success");
            post1.setPostType("experience");
            post1.setTags(Arrays.asList("success", "fruit"));
            post1.setLikesCount(10);
            post1.setIsApproved(true);
            post1.setCreatedAt(Instant.now().minus(1, ChronoUnit.DAYS));
            post1 = communityPostRepository.save(post1);

            post2 = new CommunityPost(testUser2, "Need help with vegetables");
            post2.setTitle("Vegetable Struggles");
            post2.setPostType("question");
            post2.setTags(Arrays.asList("help", "vegetables"));
            post2.setLikesCount(5);
            post2.setIsApproved(true);
            post2.setCreatedAt(Instant.now().minus(12, ChronoUnit.HOURS));
            post2 = communityPostRepository.save(post2);

            post3 = new CommunityPost(testUser1, "Peanut allergy concerns", testFood2, 1);
            post3.setTitle("Allergy Warning");
            post3.setPostType("support");
            post3.setTags(Arrays.asList("allergy", "warning"));
            post3.setLikesCount(15);
            post3.setIsApproved(false);
            post3.setIsFlagged(true);
            post3.setCreatedAt(Instant.now().minus(6, ChronoUnit.HOURS));
            post3 = communityPostRepository.save(post3);

            post4 = new CommunityPost(testUser2, "Celebration time!");
            post4.setTitle("Milestone Reached");
            post4.setPostType("celebration");
            post4.setTags(Arrays.asList("milestone", "celebration"));
            post4.setLikesCount(20);
            post4.setIsApproved(true);
            post4.setCreatedAt(Instant.now());
            post4 = communityPostRepository.save(post4);
        }

        @Test
        @DisplayName("Should find posts by author")
        void shouldFindPostsByAuthor() {
            // When
            List<CommunityPost> user1Posts = communityPostRepository.findByAuthor(testUser1);
            List<CommunityPost> user2Posts = communityPostRepository.findByAuthor(testUser2);

            // Then
            assertThat(user1Posts).hasSize(2); // post1 and post3
            assertThat(user2Posts).hasSize(2); // post2 and post4
        }

        @Test
        @DisplayName("Should find posts by type")
        void shouldFindPostsByType() {
            // When
            List<CommunityPost> experiencePosts = communityPostRepository.findByPostType("experience");
            List<CommunityPost> questionPosts = communityPostRepository.findByPostType("question");

            // Then
            assertThat(experiencePosts).hasSize(1);
            assertThat(experiencePosts.get(0).getTitle()).isEqualTo("Apple Success");
            
            assertThat(questionPosts).hasSize(1);
            assertThat(questionPosts.get(0).getTitle()).isEqualTo("Vegetable Struggles");
        }

        @Test
        @DisplayName("Should find posts by related food")
        void shouldFindPostsByRelatedFood() {
            // When
            List<CommunityPost> applePosts = communityPostRepository.findByRelatedFood(testFood1);
            List<CommunityPost> peanutPosts = communityPostRepository.findByRelatedFood(testFood2);
            List<CommunityPost> applePostsById = communityPostRepository.findByRelatedFoodId(testFood1.getId());

            // Then
            assertThat(applePosts).hasSize(1);
            assertThat(applePosts.get(0).getTitle()).isEqualTo("Apple Success");
            
            assertThat(peanutPosts).hasSize(1);
            assertThat(peanutPosts.get(0).getTitle()).isEqualTo("Allergy Warning");
            
            assertThat(applePostsById).hasSize(1);
        }

        @Test
        @DisplayName("Should find posts by tags")
        void shouldFindPostsByTags() {
            // When
            List<CommunityPost> successPosts = communityPostRepository.findByTagsContaining("success");
            List<CommunityPost> helpPosts = communityPostRepository.findByTagsContaining("help");
            List<CommunityPost> multipleTags = communityPostRepository.findByTagsIn(Arrays.asList("success", "celebration"));

            // Then
            assertThat(successPosts).hasSize(1);
            assertThat(successPosts.get(0).getTitle()).isEqualTo("Apple Success");
            
            assertThat(helpPosts).hasSize(1);
            assertThat(helpPosts.get(0).getTitle()).isEqualTo("Vegetable Struggles");
            
            assertThat(multipleTags).hasSizeGreaterThanOrEqualTo(2); // Posts with either tag
        }

        @Test
        @DisplayName("Should find posts by approval status")
        void shouldFindPostsByApprovalStatus() {
            // When
            List<CommunityPost> approvedPosts = communityPostRepository.findByIsApproved(true);
            List<CommunityPost> unapprovedPosts = communityPostRepository.findByIsApproved(false);
            List<CommunityPost> flaggedUnapproved = communityPostRepository.findByIsApprovedAndIsFlagged(false, true);

            // Then
            assertThat(approvedPosts).hasSize(3); // post1, post2, post4
            assertThat(unapprovedPosts).hasSize(1); // post3
            assertThat(flaggedUnapproved).hasSize(1); // post3
        }

        @Test
        @DisplayName("Should find posts by time")
        void shouldFindPostsByTime() {
            // When
            Instant cutoff = Instant.now().minus(12, ChronoUnit.HOURS);
            List<CommunityPost> recentPosts = communityPostRepository.findByCreatedAtAfter(cutoff);

            // Then
            assertThat(recentPosts).hasSize(3); // post2, post3, post4
        }

        @Test
        @DisplayName("Should find posts in date range")
        void shouldFindPostsInDateRange() {
            // When
            Instant start = Instant.now().minus(1, ChronoUnit.DAYS);
            Instant end = Instant.now().minus(6, ChronoUnit.HOURS);
            List<CommunityPost> rangePosts = communityPostRepository.findByCreatedAtBetween(start, end);

            // Then
            assertThat(rangePosts).hasSize(2); // post1 and post2
        }

        @Test
        @DisplayName("Should handle complex queries")
        void shouldHandleComplexQueries() {
            // When - Find approved posts by specific author with high engagement
            List<CommunityPost> user1ApprovedPosts = communityPostRepository.findByAuthor(testUser1).stream()
                .filter(post -> post.getIsApproved() && post.getLikesCount() >= 10)
                .toList();

            // Then
            assertThat(user1ApprovedPosts).hasSize(1); // Only post1 matches
            assertThat(user1ApprovedPosts.get(0).getTitle()).isEqualTo("Apple Success");
        }
    }

    @Test
    @DisplayName("Should test cross-repository relationships")
    void shouldTestCrossRepositoryRelationships() {
        // Create a food experience
        FoodExperience experience = new FoodExperience(testChild1, testFood1, 5, "Amazing!");
        experience = foodExperienceRepository.save(experience);

        // Create a community post about the same food
        CommunityPost post = new CommunityPost(testUser1, "My child loves this!", testFood1, 5);
        post = communityPostRepository.save(post);

        // Verify relationships are maintained
        Optional<FoodExperience> savedExperience = foodExperienceRepository.findById(experience.getId());
        Optional<CommunityPost> savedPost = communityPostRepository.findById(post.getId());

        assertThat(savedExperience).isPresent();
        assertThat(savedExperience.get().getChild().getId()).isEqualTo(testChild1.getId());
        assertThat(savedExperience.get().getFood().getId()).isEqualTo(testFood1.getId());

        assertThat(savedPost).isPresent();
        assertThat(savedPost.get().getAuthor().getId()).isEqualTo(testUser1.getId());
        assertThat(savedPost.get().getRelatedFood().getId()).isEqualTo(testFood1.getId());

        // Test cascade operations
        List<FoodExperience> childExperiences = foodExperienceRepository.findByChild(testChild1);
        List<CommunityPost> userPosts = communityPostRepository.findByAuthor(testUser1);
        List<FoodExperience> foodExperiences = foodExperienceRepository.findByFood(testFood1);
        List<CommunityPost> foodPosts = communityPostRepository.findByRelatedFood(testFood1);

        assertThat(childExperiences).isNotEmpty();
        assertThat(userPosts).isNotEmpty();
        assertThat(foodExperiences).isNotEmpty();
        assertThat(foodPosts).isNotEmpty();
    }
}
