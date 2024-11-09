package implement.lld.post;

import implement.lld.user.User;

import java.util.Set;

public class Comment extends Post {

    public Comment(long id, String content, User owner, Set<Tag> tags) {
        super(id, content, owner, tags);
    }
}
