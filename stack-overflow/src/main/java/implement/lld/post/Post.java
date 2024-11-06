package implement.lld.post;

import implement.lld.User;

import java.sql.Timestamp;
import java.util.List;

public abstract class Post {
    private final int id;
    private String title;
    private String content;
    private int upVoteCount;
    private int downVoteCount;
    private final User owner;
    private final Timestamp createdAt;
    private Timestamp updatedAt;
    private List<Tag> tags;

    public Post(int id, String title, String content, User owner, List<Tag> tags) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.owner = owner;
        this.tags = tags;
        this.upVoteCount = 0;
        this.downVoteCount = 0;
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.updatedAt = null;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public int getUpVoteCount() {
        return upVoteCount;
    }

    public int getDownVoteCount() {
        return downVoteCount;
    }

    public User getOwner() {
        return owner;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void incrementUpVoteCount() {
        upVoteCount++;
    }

    public void incrementDownVoteCount() {
        downVoteCount++;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
