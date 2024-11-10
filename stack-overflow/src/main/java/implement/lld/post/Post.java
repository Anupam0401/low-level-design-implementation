package implement.lld.post;

import implement.lld.user.User;

import java.sql.Timestamp;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

// Composite Pattern for Hierarchical Structure
public abstract class Post {
    private final long id;
    private String content;
    private final AtomicInteger upVoteCount;
    private final AtomicInteger downVoteCount;
    private final User owner;
    private final Timestamp createdAt;
    private Timestamp updatedAt;
    private final Set<Tag> tags;

    public Post(long id, String content, User owner, Set<Tag> tags) {
        this.id = id;
        this.content = content;
        this.owner = owner;
        this.tags = tags;
        this.upVoteCount = new AtomicInteger(0);
        this.downVoteCount = new AtomicInteger(0);
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.updatedAt = null;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public AtomicInteger getUpVoteCount() {
        return upVoteCount;
    }

    public AtomicInteger getDownVoteCount() {
        return downVoteCount;
    }

    public User getOwner() {
        return owner;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void incrementUpVoteCount() {
        this.upVoteCount.incrementAndGet();
    }

    public void incrementDownVoteCount() {
        this.downVoteCount.incrementAndGet();
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", upVoteCount=" + upVoteCount +
                ", downVoteCount=" + downVoteCount +
                ", owner=" + owner +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", tags=" + tags +
                '}';
    }
}
