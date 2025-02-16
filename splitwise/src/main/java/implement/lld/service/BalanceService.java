package implement.lld.service;

import implement.lld.model.Balance;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Service
public class BalanceService {
    private final ConcurrentHashMap<UUID, Balance> userBalances = new ConcurrentHashMap<>();

    public double getBalance(UUID userId1, UUID userId2) {
        return userBalances.getOrDefault(userId1, new Balance(userId1)).getBalance(userId2);
    }

    public void updateBalance(UUID payerId, UUID payeeId, Double amount) {
        userBalances.computeIfAbsent(payerId, Balance::new);
        userBalances.computeIfAbsent(payeeId, Balance::new);
        userBalances.get(payerId).updateBalance(payeeId, -amount);
        userBalances.get(payeeId).updateBalance(payerId, amount);
        log.info("Updated balance between {} and {} by {}", payerId, payeeId, amount);
    }

    public ConcurrentHashMap<UUID, Double> getUserBalances(UUID userId) {
        return userBalances.getOrDefault(userId, new Balance(userId)).getBalances();
    }

}
