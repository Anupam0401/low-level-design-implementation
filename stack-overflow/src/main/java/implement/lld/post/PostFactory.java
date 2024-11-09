package implement.lld.post;

import implement.lld.IdGenerator;
import implement.lld.exception.IllegalPostException;
import implement.lld.user.User;

import java.util.Set;

public class PostFactory {
    public static Post createPost(PostType type, String title, String content, User owner, Set<Tag> tags) {
        long postId = IdGenerator.generatePostId();
        if (type == PostType.QUESTION) {
            return new Question(postId, title, content, owner, tags);
        } else {
            throw new IllegalPostException("Invalid post type");
        }
    }

    public static Post createPost(PostType type, String title, String content, User owner, Set<Tag> tags, Post parentPost) {
        long postId = IdGenerator.generatePostId();
        return switch (type) {
            case QUESTION -> new Question(postId, title, content, owner, tags);
            case ANSWER -> new Answer(postId, content, owner, tags, (Question) parentPost);
            case COMMENT -> new Comment(postId, content, owner, tags, parentPost);
        };
    }
}
