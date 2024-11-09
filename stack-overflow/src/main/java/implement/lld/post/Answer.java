package implement.lld.post;

import implement.lld.user.User;

import java.util.List;
import java.util.Set;

public class Answer extends Post {
    List<Comment> comments;

    public Answer(long id, String content, User owner, Set<Tag> tags) {
        super(id, content, owner, tags);
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}
