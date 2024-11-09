package implement.lld;

public enum ReputationEvent {
    QUESTION_UPVOTE(5),
    QUESTION_DOWNVOTE(-2),
    ANSWER_UPVOTE(10),
    ANSWER_DOWNVOTE(-2),
    QUESTION_ADDED(2),
    ANSWER_ADDED(4);

    private final int reputationValue;

    ReputationEvent(int reputationValue) {
        this.reputationValue = reputationValue;
    }

    public int getReputationValue() {
        return reputationValue;
    }
}
