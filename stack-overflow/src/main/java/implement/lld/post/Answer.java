package implement.lld.post;

import implement.lld.User;

import java.util.List;

public class Answer extends Post {
    List<Comment> comments;
    List<Tag> tags;

    public Answer(int id, String title, String content, User owner) {
        super(id, title, content, owner);
    }
}
