package implement.lld.post;

import implement.lld.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Answer extends Post {
    private final List<Comment> comments;
    private final Question parentQuestion;

    public Answer(long id, String content, User owner, Set<Tag> tags, Question parentQuestion) {
        super(id, content, owner, tags);
        this.parentQuestion = parentQuestion;
        this.comments = new ArrayList<>();
    }

    public Question getParentQuestion() {
        return parentQuestion;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }
}
