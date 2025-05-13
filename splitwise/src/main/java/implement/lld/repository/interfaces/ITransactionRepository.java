package implement.lld.repository.interfaces;

import implement.lld.model.Transaction;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Transaction entity operations
 */
public interface ITransactionRepository {
    /**
     * Save a transaction to the repository
     * @param transaction the transaction to save
     */
    void saveTransaction(Transaction transaction);
    
    /**
     * Find transactions by user ID
     * @param userId the user ID
     * @return list of transactions
     */
    List<Transaction> findTransactionsByUserId(UUID userId);
    
    /**
     * Find a transaction by ID
     * @param transactionId the transaction ID
     * @return the transaction if found, null otherwise
     */
    Transaction findTransactionByTransactionId(UUID transactionId);
}
