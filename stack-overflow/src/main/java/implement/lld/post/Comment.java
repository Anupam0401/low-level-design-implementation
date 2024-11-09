package implement.lld.post;

import implement.lld.user.User;

import java.util.Set;

public class Comment extends Post {
    private final Post parentPost;

    public Comment(long id, String content, User owner, Set<Tag> tags, Post parentPost) {
        super(id, content, owner, tags);
        this.parentPost = parentPost;
    }

    public Post getParentPost() {
        return parentPost;
    }
}
