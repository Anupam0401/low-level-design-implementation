package implement.lld;

import implement.lld.post.PostManager;
import implement.lld.post.PostType;
import implement.lld.user.UserManager;

public class StackOverflowApplication {
    public static void main(String[] args) {
        UserManager userManager = UserManager.getInstance();
        PostManager postManager = PostManager.getInstance();

        // add a user
        long aliceUserId = userManager.registerUser("Alice", "aliceUserId@gmail.com");
        userManager.updateUserName(aliceUserId, "Alice Wonderland");

        // create a post
        long alicePostId = postManager.createPost(
            PostType.QUESTION,
            "What is Java?",
            "I am new to Java, can someone help me?",
            userManager.findUserById(aliceUserId),
            null);
        postManager.displayPostDetails(alicePostId);
        System.out.println(postManager.getAllPosts());

        long bobUserId = userManager.registerUser("Bob", "bob@gmail.com");
        System.out.println(userManager.getUsers());

        System.out.println(userManager.findUserById(aliceUserId).getReputationScore());
        System.out.println(userManager.findUserById(bobUserId).getReputationScore());

        // bob answers alice's question
        long bobPostId = postManager.createPost(
            PostType.ANSWER,
            "Java is a programming language",
            "Java is a high-level, class-based, object-oriented programming language",
            userManager.findUserById(bobUserId),
            null, alicePostId);
        postManager.displayPostDetails(bobPostId);

        // upvote bob's answer
        postManager.upVotePost(bobPostId, aliceUserId);
        // create a comment
        long commentId = postManager.createPost(
            PostType.COMMENT,
            "This is a great answer",
            "I really liked your answer, thanks for sharing",
            userManager.findUserById(aliceUserId),
            null, bobPostId);
        postManager.displayPostDetails(commentId);

        System.out.println(userManager.findUserById(aliceUserId).getReputationScore());
        System.out.println(userManager.findUserById(bobUserId).getReputationScore());

        System.out.println(postManager.getAllPosts());

        // create another user
        long charlieUserId = userManager.registerUser("Charlie", "charlie@gmail.com");
        // upvote on alice's question
        postManager.upVotePost(alicePostId, charlieUserId);

        postManager.displayPostDetails(alicePostId);
        postManager.displayPostDetails(bobPostId);

        // answer alice's question
        long charliePostId = postManager.createPost(
            PostType.ANSWER,
            "Java is a programming language",
            "Java is a high-level, class-based, object-oriented programming language",
            userManager.findUserById(charlieUserId),
            null, alicePostId);

        // bob downvotes charlie's answer
        postManager.downVotePost(charliePostId, bobUserId);
        postManager.downVotePost(charliePostId, aliceUserId);
        postManager.displayPostDetails(charliePostId);

        userManager.getUsers().forEach(System.out::println);

        postManager.getAllPosts().forEach(System.out::println);
    }
}