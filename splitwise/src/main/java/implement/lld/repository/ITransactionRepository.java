package implement.lld.repository;

import implement.lld.model.Transaction;

import java.util.List;
import java.util.UUID;

public interface ITransactionRepository {
    void saveTransaction(Transaction transaction);
    List<Transaction> findTransactionsByUserId(UUID userId);
    Transaction findTransactionByTransactionId(UUID transactionId);
}
