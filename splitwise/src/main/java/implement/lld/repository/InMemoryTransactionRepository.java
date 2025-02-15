package implement.lld.repository;

import implement.lld.model.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryTransactionRepository implements ITransactionRepository {
    private final ConcurrentHashMap<UUID, List<Transaction>> transactionStore = new ConcurrentHashMap<>();

    @Override
    public void saveTransaction(Transaction transaction) {
        transactionStore.computeIfAbsent(transaction.getSenderId(), k ->new CopyOnWriteArrayList<>()).add(transaction);
        transactionStore.computeIfAbsent(transaction.getReceiverId(), k ->new CopyOnWriteArrayList<>()).add(transaction);
    }

    @Override
    public List<Transaction> findTransactionsByUserId(UUID userId) {
        return transactionStore.getOrDefault(userId, new ArrayList<>());
    }

    @Override
    public Transaction findTransactionByTransactionId(UUID transactionId) {
        return transactionStore.values().stream()
                .flatMap(List::stream)
                .filter(transaction -> transaction.getTransactionId().equals(transactionId))
                .findFirst()
                .orElse(null);
    }


}
