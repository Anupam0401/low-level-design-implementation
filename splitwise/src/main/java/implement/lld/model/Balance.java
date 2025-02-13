package implement.lld.model;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Balance {
    private final UUID userId;
    private final ConcurrentHashMap<UUID, Double> balance;

    public Balance(UUID userId) {
        this.userId = userId;
        this.balance = new ConcurrentHashMap<>();
    }

    public void updateBalance(UUID userId, Double amount) {
        balance.compute(userId, (k, v) -> (v == null) ? amount : v + amount);
    }

    public double getBalance(UUID userId) {
        return balance.getOrDefault(userId, 0.0);
    }

    public ConcurrentHashMap<UUID, Double> getBalances() {
        return balance;
    }
}
