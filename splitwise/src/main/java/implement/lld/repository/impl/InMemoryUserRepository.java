package implement.lld.repository.impl;

import implement.lld.model.User;
import implement.lld.repository.interfaces.IUserRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of the IUserRepository interface
 */
@Repository
public class InMemoryUserRepository implements IUserRepository {
    private final ConcurrentHashMap<UUID, User> users = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, User> usersByEmail = new ConcurrentHashMap<>();

    @Override
    public User save(User user) {
        users.put(user.getId(), user);
        usersByEmail.put(user.getEmail(), user);
        return user;
    }

    @Override
    public User findById(UUID userId) {
        return users.get(userId);
    }

    @Override
    public User findByEmail(String email) {
        return usersByEmail.get(email);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public boolean deleteById(UUID userId) {
        User user = users.remove(userId);
        if (user != null) {
            usersByEmail.remove(user.getEmail());
            return true;
        }
        return false;
    }
}
