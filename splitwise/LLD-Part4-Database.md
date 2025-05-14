# Splitwise Low-Level Design - Part 4: Database Schema

This document outlines the database schema design for the Splitwise application, focusing on entity relationships, table structures, and data access patterns.

## 1. Entity Relationship Diagram (ERD)

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│    Users    │       │   Groups    │       │  Expenses   │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ user_id (PK)│◄──┐   │ group_id (PK)│◄─┐    │ expense_id (PK)│
│ name        │   │   │ name        │  │    │ amount      │
│ email       │   │   │ description │  │    │ description │
│ created_at  │   │   │ owner_id (FK)│──┘    │ payer_id (FK)│──┐
│ updated_at  │   │   │ created_at  │       │ group_id (FK)│──┼─┐
└─────────────┘   │   │ updated_at  │       │ expense_type │  │ │
                  │   └─────────────┘       │ created_at   │  │ │
                  │         │               └─────────────┘  │ │
                  │         │                      │         │ │
                  │         │                      │         │ │
                  │         ▼                      ▼         │ │
                  │   ┌─────────────┐       ┌─────────────┐  │ │
                  └───┤Group_Members│       │   Splits    │  │ │
                      ├─────────────┤       ├─────────────┤  │ │
                      │ group_id (FK)│       │ split_id (PK)│  │ │
                      │ user_id (FK)│◄──────│ expense_id (FK)│◄┘ │
                      │ joined_at   │       │ user_id (FK)│◄───┘
                      └─────────────┘       │ amount      │
                                           │ percent     │
                                           │ split_type  │
                                           └─────────────┘
                                                  
┌─────────────┐       ┌─────────────┐
│Transactions │       │  Balances   │
├─────────────┤       ├─────────────┤
│transaction_id (PK)│  │ balance_id (PK)│
│ sender_id (FK)│─┐    │ user_id1 (FK)│──┐
│ receiver_id (FK)│┼──┐ │ user_id2 (FK)│──┼─┐
│ amount      │  │  │ │ amount      │  │ │
│ created_at  │  │  │ └─────────────┘  │ │
└─────────────┘  │  │                  │ │
                 │  │                  │ │
                 │  └──────────────────┘ │
                 │                       │
                 └───────────────────────┘
```

## 2. Table Definitions

### 2.1 Users Table

```sql
CREATE TABLE users (
    user_id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 2.2 Groups Table

```sql
CREATE TABLE groups (
    group_id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    owner_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(user_id)
);
```

### 2.3 Group_Members Table

```sql
CREATE TABLE group_members (
    group_id UUID NOT NULL,
    user_id UUID NOT NULL,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (group_id, user_id),
    FOREIGN KEY (group_id) REFERENCES groups(group_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
```

### 2.4 Expenses Table

```sql
CREATE TABLE expenses (
    expense_id UUID PRIMARY KEY,
    amount DECIMAL(10, 2) NOT NULL,
    description TEXT,
    payer_id UUID NOT NULL,
    group_id UUID,
    expense_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (payer_id) REFERENCES users(user_id),
    FOREIGN KEY (group_id) REFERENCES groups(group_id)
);
```

### 2.5 Splits Table

```sql
CREATE TABLE splits (
    split_id UUID PRIMARY KEY,
    expense_id UUID NOT NULL,
    user_id UUID NOT NULL,
    amount DECIMAL(10, 2),
    percent DECIMAL(5, 2),
    split_type VARCHAR(20) NOT NULL,
    FOREIGN KEY (expense_id) REFERENCES expenses(expense_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
```

### 2.6 Transactions Table

```sql
CREATE TABLE transactions (
    transaction_id UUID PRIMARY KEY,
    sender_id UUID NOT NULL,
    receiver_id UUID NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(user_id),
    FOREIGN KEY (receiver_id) REFERENCES users(user_id)
);
```

### 2.7 Balances Table

```sql
CREATE TABLE balances (
    balance_id UUID PRIMARY KEY,
    user_id1 UUID NOT NULL,
    user_id2 UUID NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (user_id1) REFERENCES users(user_id),
    FOREIGN KEY (user_id2) REFERENCES users(user_id),
    CONSTRAINT unique_user_pair UNIQUE (user_id1, user_id2),
    CONSTRAINT different_users CHECK (user_id1 <> user_id2)
);
```

## 3. Indexes

```sql
-- Users table indexes
CREATE INDEX idx_users_email ON users(email);

-- Groups table indexes
CREATE INDEX idx_groups_owner ON groups(owner_id);

-- Group_Members table indexes
CREATE INDEX idx_group_members_user ON group_members(user_id);

-- Expenses table indexes
CREATE INDEX idx_expenses_payer ON expenses(payer_id);
CREATE INDEX idx_expenses_group ON expenses(group_id);
CREATE INDEX idx_expenses_created_at ON expenses(created_at);

-- Splits table indexes
CREATE INDEX idx_splits_expense ON splits(expense_id);
CREATE INDEX idx_splits_user ON splits(user_id);

-- Transactions table indexes
CREATE INDEX idx_transactions_sender ON transactions(sender_id);
CREATE INDEX idx_transactions_receiver ON transactions(receiver_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);

-- Balances table indexes
CREATE INDEX idx_balances_user1 ON balances(user_id1);
CREATE INDEX idx_balances_user2 ON balances(user_id2);
```

## 4. Data Access Patterns

### 4.1 Common Queries

#### Get User Balances
```sql
SELECT b.user_id2 as other_user_id, b.amount
FROM balances b
WHERE b.user_id1 = :userId;
```

#### Get Group Expenses
```sql
SELECT e.*, s.*
FROM expenses e
JOIN splits s ON e.expense_id = s.expense_id
WHERE e.group_id = :groupId
ORDER BY e.created_at DESC;
```

#### Get User Transactions
```sql
SELECT t.*
FROM transactions t
WHERE t.sender_id = :userId OR t.receiver_id = :userId
ORDER BY t.created_at DESC;
```

#### Get Group Members
```sql
SELECT u.*
FROM users u
JOIN group_members gm ON u.user_id = gm.user_id
WHERE gm.group_id = :groupId;
```

### 4.2 Transaction Management

For operations that update multiple tables, such as adding an expense and updating balances, transactions are used to ensure data consistency:

```java
@Transactional
public Expense createExpense(UUID payerId, double amount, String description, 
                           List<Split> splits, SplitType splitType) {
    // Create expense
    Expense expense = expenseRepository.createExpense(payerId, amount, description, 
                                                   splits, splitType);
    
    // Update balances
    for (Split split : splits) {
        if (!split.getUserId().equals(payerId)) {
            balanceService.updateBalance(split.getUserId(), payerId, split.getAmount());
        }
    }
    
    return expense;
}
```

## 5. Database Migration Strategy

The application is designed to support easy migration from in-memory storage to a persistent database:

1. **Repository Interfaces**: All data access is done through repository interfaces, allowing different implementations.

2. **Spring Data JPA**: For SQL databases, Spring Data JPA repositories can be implemented:

```java
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    UserEntity findByEmail(String email);
}

@Repository
@Primary
public class JpaUserRepository implements IUserRepository {
    private final UserJpaRepository userJpaRepository;
    
    // Implementation methods that use the JPA repository
}
```

3. **MongoDB Repositories**: For NoSQL databases, MongoDB repositories can be implemented:

```java
public interface UserMongoRepository extends MongoRepository<UserDocument, UUID> {
    UserDocument findByEmail(String email);
}

@Repository
@Primary
public class MongoUserRepository implements IUserRepository {
    private final UserMongoRepository userMongoRepository;
    
    // Implementation methods that use the MongoDB repository
}
```

## 6. Data Consistency and Integrity

### 6.1 Constraints

- **Foreign Key Constraints**: Ensure referential integrity between related tables
- **Unique Constraints**: Prevent duplicate entries (e.g., user email)
- **Check Constraints**: Enforce business rules (e.g., different users in balance entries)

### 6.2 Optimistic Locking

For concurrent updates to the same data, optimistic locking is implemented:

```java
@Entity
public class Balance {
    @Version
    private Long version;
    
    // Other fields
}
```

### 6.3 Audit Trail

For tracking changes to critical data:

```java
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Expense {
    @CreatedDate
    private LocalDateTime createdAt;
    
    @CreatedBy
    private String createdBy;
    
    @LastModifiedDate
    private LocalDateTime lastModifiedAt;
    
    @LastModifiedBy
    private String lastModifiedBy;
    
    // Other fields
}
```

## 7. Performance Considerations

### 7.1 Connection Pooling

```properties
# HikariCP connection pool configuration
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.connection-timeout=30000
```

### 7.2 Query Optimization

- Use of indexed columns in WHERE clauses
- Pagination for large result sets
- Eager/lazy loading strategies for entity relationships

### 7.3 Caching

```java
@Configuration
@EnableCaching
public class CachingConfig {
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
            new ConcurrentMapCache("users"),
            new ConcurrentMapCache("groups"),
            new ConcurrentMapCache("balances")
        ));
        return cacheManager;
    }
}

@Service
public class UserService {
    @Cacheable("users")
    public User getUserById(UUID userId) {
        // Database access
    }
    
    @CacheEvict(value = "users", key = "#user.userId")
    public void updateUser(User user) {
        // Database update
    }
}
```

This completes the database schema design aspect of the Low-Level Design for the Splitwise application.
