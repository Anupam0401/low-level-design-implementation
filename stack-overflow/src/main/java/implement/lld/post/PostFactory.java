package implement.lld.post;

import implement.lld.IdGenerator;
import implement.lld.exception.IllegalPostException;
import implement.lld.user.User;

import java.util.Set;

public class PostFactory {
    public static Post createPost(PostType type, String title, String content, User owner, Set<Tag> tags) {
        if (!isValidPost(type, title, content, owner)) {
            throw new IllegalPostException("Invalid post details, unable to create post");
        }
        long postId = IdGenerator.generatePostId();
        return switch (type) {
            case QUESTION -> new Question(postId, title, content, owner, tags);
            case ANSWER -> new Answer(postId, content, owner, tags);
            case COMMENT -> new Comment(postId, content, owner, tags);
        };
    }

    private static boolean isValidPost(PostType type, String title, String content, User owner) {
        return PostType.getAllPostTypes().contains(type)
            && title != null
            && !title.isEmpty()
            && content != null
            && !content.isEmpty()
            && owner != null;
    }
}
