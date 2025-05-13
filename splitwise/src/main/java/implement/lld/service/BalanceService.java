package implement.lld.service;

import implement.lld.repository.interfaces.IBalanceRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Service
public class BalanceService {
    private final IBalanceRepository balanceRepository;

    @Autowired
    public BalanceService(IBalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    public double getBalance(UUID userId1, UUID userId2) {
        return balanceRepository.getBalance(userId1, userId2);
    }

    public void updateBalance(UUID payerId, UUID payeeId, Double amount) {
        balanceRepository.updateBalance(payerId, payeeId, amount);
        log.info("Updated balance between {} and {} by {}", payerId, payeeId, amount);
    }

    public ConcurrentHashMap<UUID, Double> getUserBalances(UUID userId) {
        return balanceRepository.getUserBalances(userId);
    }
}
