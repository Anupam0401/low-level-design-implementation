package implement.lld.repository.interfaces;

import implement.lld.model.User;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for User entity operations
 */
public interface IUserRepository {
    /**
     * Save a user to the repository
     * @param user the user to save
     * @return the saved user
     */
    User save(User user);
    
    /**
     * Find a user by ID
     * @param userId the user ID
     * @return the user if found, null otherwise
     */
    User findById(UUID userId);
    
    /**
     * Find a user by email
     * @param email the email
     * @return the user if found, null otherwise
     */
    User findByEmail(String email);
    
    /**
     * Find all users
     * @return list of all users
     */
    List<User> findAll();
    
    /**
     * Delete a user by ID
     * @param userId the user ID
     * @return true if deleted, false otherwise
     */
    boolean deleteById(UUID userId);
}
