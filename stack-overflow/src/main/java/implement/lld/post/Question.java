package implement.lld.post;

import implement.lld.User;

import java.util.List;

public class Question extends Post {
    List<Answer> answers;
    List<Comment> comments;
    List<Tag> tags;

    public Question(int id, String title, String content, User owner) {
        super(id, title, content, owner);
    }
}
