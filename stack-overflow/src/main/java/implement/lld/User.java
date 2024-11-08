package implement.lld;

import java.sql.Timestamp;

public class User {
    private int id = 1;
    private String name;
    private String email;
    private final Timestamp createdAt;
    private Timestamp updatedAt;
    private double reputationScore;

    public User(String name, String email) {
        this.id = id++;
        this.name = name;
        this.email = email;
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.updatedAt = null;
        this.reputationScore = 0;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public double getReputationScore() {
        return reputationScore;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setReputationScore(double reputationScore) {
        this.reputationScore = reputationScore;
    }
}
