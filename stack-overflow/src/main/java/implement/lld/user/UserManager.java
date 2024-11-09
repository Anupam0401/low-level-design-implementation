package implement.lld.user;

import implement.lld.IdGenerator;
import implement.lld.exception.IllegalUserException;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {
    private static volatile UserManager instance;
    private final ConcurrentHashMap<Long, User> users = new ConcurrentHashMap<>();

    private UserManager() {
    }

    public static UserManager getInstance() {
        if (instance == null) {
            synchronized (UserManager.class) {
                if (instance == null) {
                    instance = new UserManager();
                }
            }
        }
        return instance;
    }

    public long registerUser(String name, String email) {
        validateUser(name, email);
        long userId = IdGenerator.generateUserId();
        User user = new User(userId, name, email);
        users.put(userId, user);
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        System.out.println("User registered successfully with userId: " + userId + "\n");
        return userId;
    }

    private void validateUser(String name, String email) {
        if (name == null || name.isEmpty() || email == null || email.isEmpty()) {
            throw new IllegalUserException("Name and email are mandatory fields");
        }
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalUserException("Invalid email");
        }
        if (findUserByName(name) != null) {
            throw new IllegalUserException("User with same name already exists, please use different name");
        }
    }

    public void removeUser(User user) {
        users.remove(user.getId());
        System.out.println("User removed successfully\n");
    }

    public void updateUserName(long userId, String updatedName) {
        User user = users.get(userId);
        if (user == null) {
            throw new IllegalUserException("User not found");
        }
        user.setName(updatedName);
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        System.out.println("Name for userId: " + userId + " updated successfully\n");
    }

    public void updateUserEmail(long userId, String updatedEmail) {
        User user = users.get(userId);
        if (user == null) {
            throw new IllegalUserException("User not found");
        }
        user.setEmail(updatedEmail);
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        System.out.println("Email updated successfully\n");
    }

    public User findUserById(long userId) {
        if (!users.containsKey(userId)) {
            throw new IllegalUserException("User not found");
        }
        return users.get(userId);
    }

    public User findUserByName(String name) {
        for (User user : users.values()) {
            if (user.getName().equals(name)) {
                return user;
            }
        }
        return null;
    }

    public List<Map<Long, User>> getUsers() {
        List<Map<Long, User>> userList = new ArrayList<>();
        for (User user : users.values()) {
            userList.add(Map.of(user.getId(), user));
        }
        return userList;
    }

}
