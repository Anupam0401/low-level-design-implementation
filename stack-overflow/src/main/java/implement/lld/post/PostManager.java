package implement.lld.post;

import implement.lld.ReputationCalculator;
import implement.lld.ReputationEvent;
import implement.lld.exception.IllegalPostException;
import implement.lld.user.User;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PostManager {
    private static volatile PostManager instance;
    private final ReputationCalculator reputationCalculator;
    private final ConcurrentHashMap<Long, Post> posts = new ConcurrentHashMap<>();

    private PostManager() {
        this.reputationCalculator = ReputationCalculator.getInstance();
    }

    public static PostManager getInstance() {
        if (instance == null) {
            synchronized (PostManager.class) {
                if (instance == null) {
                    instance = new PostManager();
                }
            }
        }
        return instance;
    }

    public void createPost(PostType type, String title, String content, User owner, Set<Tag> tags) {
        Post post = PostFactory.createPost(type, title, content, owner, tags);
        switch (post) {
            case Question ignored -> {
                reputationCalculator.updateReputationScore(owner, ReputationEvent.QUESTION_ADDED);
                System.out.println("Question created successfully\n");
            }
            case Answer ignored -> {
                reputationCalculator.updateReputationScore(owner, ReputationEvent.ANSWER_ADDED);
                System.out.println("Answer created successfully\n");
            }
            case Comment ignored -> System.out.println("Comment created successfully\n");
            default -> throw new IllegalStateException("Unexpected value: " + post);
        }
        posts.put(post.getId(), post);
    }

    public void updatePostContent(long postId, String updatedContent) {
        Post post = posts.get(postId);
        if (post == null) {
            throw new IllegalPostException("Post not found\n");
        }
        post.setContent(updatedContent);
        post.setUpdatedAt(Timestamp.from(Instant.now()));
        System.out.println("Content updated successfully\n");
    }

    public void deletePost(long postId) {
        posts.remove(postId);
    }

    public void updatePostTitle(long postId, String updatedTitle) {
        Post post = posts.get(postId);
        if (post == null) {
            throw new IllegalPostException("Post not found");
        }
        if (post instanceof Question) {
            ((Question) post).setTitle(updatedTitle);
            post.setUpdatedAt(Timestamp.from(Instant.now()));
            System.out.println("Title updated successfully\n");
        } else {
            throw new IllegalPostException("Post is not a question");
        }
    }

    public void addAnswerToQuestion(long questionId, Answer answer) {
        Post post = posts.get(questionId);
        if (post == null) {
            throw new IllegalPostException("Question not found");
        }
        if (post instanceof Question) {
            ((Question) post).getAnswers().add(answer);
            post.setUpdatedAt(Timestamp.from(Instant.now()));
            System.out.println("Answer added successfully\n");
        } else {
            throw new IllegalPostException("Post is not a question");
        }
    }

    public void addCommentToPost(long postId, Comment comment) {
        Post post = posts.get(postId);
        switch (post) {
            case Question question -> {
                question.getComments().add(comment);
                question.setUpdatedAt(Timestamp.from(Instant.now()));
                System.out.println("Comment added successfully\n");
            }
            case Answer answer -> {
                answer.getComments().add(comment);
                answer.setUpdatedAt(Timestamp.from(Instant.now()));
                System.out.println("Comment added successfully\n");
            }
            case null -> throw new IllegalPostException("Post not found");
            default -> throw new IllegalPostException("Post is not a question or answer");
        }
    }

    public void addTagToPost(long postId, Tag tag) {
        Post post = posts.get(postId);
        if (post == null) {
            throw new IllegalPostException("Post not found");
        }
        if (post.getTags().add(tag)) {
            post.setUpdatedAt(Timestamp.from(Instant.now()));
            System.out.println("Tag added successfully\n");
        } else {
            System.out.println("Tag already exists");
        }
    }

    public void upVotePost(long postId) {
        Post post = posts.get(postId);
        if (post == null) {
            throw new IllegalPostException("Post not found");
        }
        post.getUpVoteCount().incrementAndGet();
        post.setUpdatedAt(Timestamp.from(Instant.now()));
        if (post instanceof Question question) {
            reputationCalculator.updateReputationScore(question.getOwner(), ReputationEvent.QUESTION_UPVOTE);
        } else if (post instanceof Answer answer) {
            reputationCalculator.updateReputationScore(answer.getOwner(), ReputationEvent.ANSWER_UPVOTE);
        }
        System.out.println("Post up-voted successfully\n");
    }

    public void downVotePost(long postId) {
        Post post = posts.get(postId);
        if (post == null) {
            throw new IllegalPostException("Post not found");
        }
        post.getDownVoteCount().incrementAndGet();
        post.setUpdatedAt(Timestamp.from(Instant.now()));
        if (post instanceof Question question) {
            reputationCalculator.updateReputationScore(question.getOwner(), ReputationEvent.QUESTION_DOWNVOTE);
        } else if (post instanceof Answer answer) {
            reputationCalculator.updateReputationScore(answer.getOwner(), ReputationEvent.ANSWER_DOWNVOTE);
        }
        System.out.println("Post down-voted successfully\n");
    }

    public void displayPostDetails(long postId) {
        Post post = posts.get(postId);
        if (post == null) {
            throw new IllegalPostException("Post not found");
        }
        System.out.println("Post Details:");
        System.out.printf("%-20s: %s%n", "Post ID", post.getId());
        System.out.printf("%-20s: %s%n", "Post Content", post.getContent());
        System.out.printf("%-20s: %s%n", "Post Owner", post.getOwner().getName());
        System.out.printf("%-20s: %s%n", "Post Tags", post.getTags());
        System.out.printf("%-20s: %d%n", "Post Up-votes", post.getUpVoteCount().get());
        System.out.printf("%-20s: %d%n", "Post Down-votes", post.getDownVoteCount().get());
        System.out.printf("%-20s: %s%n", "Post Created At", post.getCreatedAt());
        System.out.printf("%-20s: %s%n", "Post Updated At", post.getUpdatedAt());
        if (post instanceof Question question) {
            System.out.println("Question Details:");
            System.out.printf("%-20s: %s%n", "Question Title", question.getTitle());
            System.out.printf("%-20s: %s%n", "Question Answers", question.getAnswers());
            System.out.printf("%-20s: %s%n", "Question Comments", question.getComments());
        } else if (post instanceof Answer answer) {
            System.out.println("Answer Details:");
            System.out.printf("%-20s: %s%n", "Answer Comments", answer.getComments());
        }
        System.out.println();
    }

    public List<Map<Long, Post>> getAllPosts() {
        List<Map<Long, Post>> allPosts = new ArrayList<>();
        for (Post post : posts.values()) {
            allPosts.add(Map.of(post.getId(), post));
        }
        return allPosts;
    }

    public List<Map<Long, Post>> getPostsByUser(User user) {
        List<Map<Long, Post>> userPosts = new ArrayList<>();
        int i = 1;
        for (Post post : posts.values()) {
            if (post.getOwner().equals(user)) {
                userPosts.add(Map.of(post.getId(), post));
            }
        }
        return userPosts;
    }

    public List<Map<Long, Post>> getPostsByTag(Tag tag) {
        List<Map<Long, Post>> tagPosts = new ArrayList<>();
        int i = 1;
        for (Post post : posts.values()) {
            if (post.getTags().contains(tag)) {
                tagPosts.add(Map.of(post.getId(), post));
            }
        }
        return tagPosts;
    }

    public List<Map<Long, Post>> getPostsByType(PostType type) {
        List<Map<Long, Post>> typePosts = new ArrayList<>();
        for (Post post : posts.values()) {
            if (post instanceof Question && type == PostType.QUESTION) {
                typePosts.add(Map.of(post.getId(), post));
            } else if (post instanceof Answer && type == PostType.ANSWER) {
                typePosts.add(Map.of(post.getId(), post));
            } else if (post instanceof Comment && type == PostType.COMMENT) {
                typePosts.add(Map.of(post.getId(), post));
            }
        }
        return typePosts;
    }
}
