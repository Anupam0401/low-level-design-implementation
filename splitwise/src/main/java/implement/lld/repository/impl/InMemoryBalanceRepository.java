package implement.lld.repository.impl;

import implement.lld.model.Balance;
import implement.lld.repository.interfaces.IBalanceRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of the IBalanceRepository interface
 */
@Repository
public class InMemoryBalanceRepository implements IBalanceRepository {
    private final ConcurrentHashMap<UUID, Balance> userBalances = new ConcurrentHashMap<>();

    @Override
    public double getBalance(UUID userId1, UUID userId2) {
        return userBalances.getOrDefault(userId1, new Balance(userId1)).getBalance(userId2);
    }

    @Override
    public void updateBalance(UUID payerId, UUID payeeId, Double amount) {
        userBalances.computeIfAbsent(payerId, Balance::new);
        userBalances.computeIfAbsent(payeeId, Balance::new);
        userBalances.get(payerId).updateBalance(payeeId, -amount);
        userBalances.get(payeeId).updateBalance(payerId, amount);
    }

    @Override
    public ConcurrentHashMap<UUID, Double> getUserBalances(UUID userId) {
        return userBalances.getOrDefault(userId, new Balance(userId)).getBalances();
    }

    @Override
    public boolean clearUserBalances(UUID userId) {
        Balance balance = userBalances.get(userId);
        if (balance != null) {
            balance.getBalances().clear();
            return true;
        }
        return false;
    }
}
