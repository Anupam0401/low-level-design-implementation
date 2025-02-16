package implement.lld.service;

import implement.lld.model.Transaction;
import implement.lld.repository.ITransactionRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Log4j2
@Service
public class TransactionService {
    private final BalanceService balanceService;
    private final ITransactionRepository transactionRepository;
    private final ConcurrentHashMap<UUID, ReentrantLock> userLocks = new ConcurrentHashMap<>();

    public TransactionService(BalanceService balanceService, ITransactionRepository transactionRepository) {
        this.balanceService = balanceService;
        this.transactionRepository = transactionRepository;
    }

    public boolean performTransaction(UUID senderId, UUID receiverId, double amount) {
        ReentrantLock lock1 = userLocks.computeIfAbsent(senderId, k -> new ReentrantLock());
        ReentrantLock lock2 = userLocks.computeIfAbsent(receiverId, k -> new ReentrantLock());

        // Lock users in a consistent order to avoid deadlocks
        ReentrantLock firstLock = senderId.compareTo(receiverId) < 0 ? lock1 : lock2;
        ReentrantLock secondLock = senderId.compareTo(receiverId) < 0 ? lock2 : lock1;

        firstLock.lock();
        try {
            secondLock.lock();
            try {
                // Perform transaction
                balanceService.updateBalance(senderId, receiverId, -amount);
                balanceService.updateBalance(receiverId, senderId, amount);

                // Record transaction
                transactionRepository.saveTransaction(Transaction.builder()
                        .transactionId(UUID.randomUUID())
                        .senderId(senderId)
                        .receiverId(receiverId)
                        .amount(amount)
                        .createdAt(Timestamp.valueOf(LocalDate.now().atStartOfDay()))
                        .build());
                log.info("Transaction successful: {} transferred ₹{} to {}", senderId, amount, receiverId);
                return true;
            } finally {
                secondLock.unlock();
            }
        } finally {
            firstLock.unlock();
        }
    }

    public List<Transaction> getAllTransactions(UUID userId) {
        return transactionRepository.findTransactionsByUserId(userId);
    }

    public Transaction getTransaction(UUID transactionId) {
        return transactionRepository.findTransactionByTransactionId(transactionId);
    }
}
