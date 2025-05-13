package implement.lld.repository.interfaces;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository interface for Balance entity operations
 */
public interface IBalanceRepository {
    /**
     * Get the balance between two users
     * @param userId1 first user ID
     * @param userId2 second user ID
     * @return the balance amount
     */
    double getBalance(UUID userId1, UUID userId2);
    
    /**
     * Update the balance between two users
     * @param payerId payer user ID
     * @param payeeId payee user ID
     * @param amount amount to update
     */
    void updateBalance(UUID payerId, UUID payeeId, Double amount);
    
    /**
     * Get all balances for a user
     * @param userId user ID
     * @return map of user IDs to balance amounts
     */
    ConcurrentHashMap<UUID, Double> getUserBalances(UUID userId);
    
    /**
     * Clear all balances for a user
     * @param userId user ID
     * @return true if cleared, false otherwise
     */
    boolean clearUserBalances(UUID userId);
}
