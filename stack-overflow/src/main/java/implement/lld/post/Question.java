package implement.lld.post;

import implement.lld.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Question extends Post {
    private final List<Answer> answers;
    private final List<Comment> comments;
    private String title;

    public Question(long id, String title, String content, User owner, Set<Tag> tags) {
        super(id, content, owner, tags);
        this.title = title;
        this.answers = new ArrayList<>();
        this.comments = new ArrayList<>();
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void addAnswer(Answer answer) {
        answers.add(answer);
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
