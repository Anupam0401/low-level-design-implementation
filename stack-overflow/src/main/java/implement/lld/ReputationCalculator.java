package implement.lld;

import implement.lld.user.User;

public class ReputationCalculator {
    private static final ReputationCalculator reputationCalculator = new ReputationCalculator();

    private ReputationCalculator() {
    }

    public static ReputationCalculator getInstance() {
        return reputationCalculator;
    }

    private double calculateReputationScore(User user, ReputationEvent reputationEvent) {
        double reputationScore = user.getReputationScore();
        reputationScore += reputationEvent.getReputationValue();
        return reputationScore;
    }

    public void updateReputationScore(User user, ReputationEvent reputationEvent) {
        double reputationScore = calculateReputationScore(user, reputationEvent);
        user.setReputationScore(reputationScore);
    }
}
