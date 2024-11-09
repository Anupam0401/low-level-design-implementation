package implement.lld.post;

import implement.lld.user.User;

import java.util.List;
import java.util.Set;

public class Question extends Post {
    List<Answer> answers;
    List<Comment> comments;
    String title;

    public Question(long id, String title, String content, User owner, Set<Tag> tags) {
        super(id, content, owner, tags);
        this.title = title;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
