package implement.lld.post;

import implement.lld.User;

import java.util.List;

public class Comment extends Post {
    List<Tag> tags;

    public Comment(int id, String title, String content, User owner) {
        super(id, title, content, owner);
    }
}
