# Splitwise Low-Level Design - Implementation Details

## 1. Design Patterns Used

### 1.1 Repository Pattern
The application implements the Repository pattern to abstract data access logic:

```
Interface (IUserRepository) ← Implementation (InMemoryUserRepository)
```

**Benefits:**
- Decouples business logic from data access
- Enables easy switching between in-memory and persistent storage
- Facilitates testing through mock repositories

**Implementation Example:**
```java
public interface IUserRepository {
    User createUser(String name, String email);
    User getUserById(UUID userId);
    List<User> getAllUsers();
}

@Repository
public class InMemoryUserRepository implements IUserRepository {
    private final ConcurrentHashMap<UUID, User> users = new ConcurrentHashMap<>();
    
    @Override
    public User createUser(String name, String email) {
        User user = new User(name, email);
        users.put(user.getUserId(), user);
        return user;
    }
    
    @Override
    public User getUserById(UUID userId) {
        return users.get(userId);
    }
    
    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
}
```

### 1.2 Strategy Pattern
Used for implementing different expense splitting strategies:

```
Interface (Split) ← Implementations (EqualSplit, PercentSplit, ExactSplit)
```

**Benefits:**
- Encapsulates different splitting algorithms
- Makes splitting strategies interchangeable
- Allows adding new strategies without modifying existing code

**Implementation Example:**
```java
public enum SplitType {
    EQUAL, PERCENT, EXACT
}

public interface Split {
    UUID getUserId();
    double getAmount();
    SplitType getSplitType();
}

public class EqualSplit implements Split {
    private final UUID userId;
    private double amount;
    
    @Override
    public SplitType getSplitType() {
        return SplitType.EQUAL;
    }
    
    // Other methods
}

public class PercentSplit implements Split {
    private final UUID userId;
    private final double percent;
    private double amount;
    
    @Override
    public SplitType getSplitType() {
        return SplitType.PERCENT;
    }
    
    // Other methods
}
```

### 1.3 Factory Pattern
Used for creating different types of expenses:

**Benefits:**
- Centralizes expense creation logic
- Encapsulates expense instantiation details
- Enables easy extension for new expense types

**Implementation Example:**
```java
public enum ExpenseType {
    PERSONAL, NON_GROUP, GROUP
}

public class ExpenseFactory {
    public static Expense createExpense(ExpenseType type, UUID payerId, double amount, 
                                       String description, List<Split> splits) {
        switch (type) {
            case PERSONAL:
                return new PersonalExpense(payerId, amount, description, splits);
            case NON_GROUP:
                return new NonGroupExpense(payerId, amount, description, splits);
            case GROUP:
                return new GroupExpense(payerId, amount, description, splits);
            default:
                throw new InvalidExpenseTypeException("Invalid expense type");
        }
    }
}
```

### 1.4 Template Method Pattern
Used in the AbstractExpenseService to define the skeleton of expense creation:

**Benefits:**
- Defines common algorithm structure in a base class
- Allows subclasses to override specific steps
- Promotes code reuse

**Implementation Example:**
```java
public abstract class AbstractExpenseService {
    protected final IExpenseRepository expenseRepository;
    protected final BalanceService balanceService;
    
    public Expense createExpense(UUID payerId, double amount, String description, 
                               List<Split> splits, SplitType splitType) {
        // Common validation
        validateAmount(amount);
        validateSplits(splits, amount, splitType);
        
        // Create expense (implemented by subclasses)
        Expense expense = createExpenseEntity(payerId, amount, description, splits);
        
        // Process expense (common logic)
        processExpense(expense);
        
        return expense;
    }
    
    protected abstract Expense createExpenseEntity(UUID payerId, double amount, 
                                                String description, List<Split> splits);
    
    protected void validateAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount should be positive");
        }
    }
    
    // Other methods
}
```

## 2. Key Algorithms

### 2.1 Debt Simplification Algorithm

The debt simplification algorithm minimizes the number of transactions needed to settle debts:

```java
public List<SimplifiedTransaction> simplifyDebts(Map<UUID, Double> balances) {
    List<SimplifiedTransaction> transactions = new ArrayList<>();
    
    // Separate users into creditors (positive balance) and debtors (negative balance)
    PriorityQueue<Map.Entry<UUID, Double>> creditors = new PriorityQueue<>(
            Comparator.comparingDouble(Map.Entry::getValue));
    PriorityQueue<Map.Entry<UUID, Double>> debtors = new PriorityQueue<>(
            Comparator.comparingDouble(Map.Entry::getValue));
    
    for (Map.Entry<UUID, Double> entry : balances.entrySet()) {
        if (Math.abs(entry.getValue()) < 0.01) {
            // Skip users with zero balance
            continue;
        }
        
        if (entry.getValue() > 0) {
            // User is owed money
            creditors.add(entry);
        } else {
            // User owes money
            debtors.add(entry);
        }
    }
    
    // Process all debts
    while (!creditors.isEmpty() && !debtors.isEmpty()) {
        Map.Entry<UUID, Double> maxCreditor = creditors.poll();
        Map.Entry<UUID, Double> maxDebtor = debtors.poll();
        
        double creditorAmount = maxCreditor.getValue();
        double debtorAmount = -maxDebtor.getValue(); // Convert to positive
        
        double transactionAmount = Math.min(creditorAmount, debtorAmount);
        
        // Create a transaction from debtor to creditor
        transactions.add(new SimplifiedTransaction(
                maxDebtor.getKey(),
                maxCreditor.getKey(),
                transactionAmount
        ));
        
        // Update remaining balances
        double creditorRemaining = creditorAmount - transactionAmount;
        double debtorRemaining = debtorAmount - transactionAmount;
        
        // If creditor still has remaining balance, add back to the queue
        if (creditorRemaining > 0.01) {
            maxCreditor.setValue(creditorRemaining);
            creditors.add(maxCreditor);
        }
        
        // If debtor still has remaining debt, add back to the queue
        if (debtorRemaining > 0.01) {
            maxDebtor.setValue(-debtorRemaining); // Convert back to negative
            debtors.add(maxDebtor);
        }
    }
    
    return transactions;
}
```

### 2.2 Balance Calculation Algorithm

The balance calculation algorithm updates balances between users after an expense:

```java
public void updateBalancesForExpense(Expense expense) {
    UUID payerId = expense.getPayerId();
    List<Split> splits = expense.getSplits();
    
    for (Split split : splits) {
        UUID userId = split.getUserId();
        double amount = split.getAmount();
        
        if (!userId.equals(payerId)) {
            // Update balance: positive means user owes payer
            balanceService.updateBalance(userId, payerId, amount);
            // Update balance: negative means payer is owed by user
            balanceService.updateBalance(payerId, userId, -amount);
        }
    }
}
```

## 3. Data Flow Diagrams

### 3.1 Adding an Expense

```
┌──────────┐     ┌───────────────┐     ┌─────────────────┐     ┌──────────────┐
│          │     │               │     │                 │     │              │
│  Client  │────▶│  Controller   │────▶│  Service Layer  │────▶│  Repository  │
│          │     │               │     │                 │     │              │
└──────────┘     └───────────────┘     └─────────────────┘     └──────────────┘
                        │                      │                       │
                        │                      │                       │
                        ▼                      ▼                       ▼
                 ┌─────────────┐       ┌─────────────────┐     ┌──────────────┐
                 │             │       │                 │     │              │
                 │  DTO Layer  │       │  Balance Update │     │  In-Memory   │
                 │             │       │                 │     │  Storage     │
                 └─────────────┘       └─────────────────┘     └──────────────┘
```

### 3.2 Simplifying Debts

```
┌──────────┐     ┌───────────────┐     ┌─────────────────────┐
│          │     │               │     │                     │
│  Client  │────▶│  Controller   │────▶│  DebtSimplification │
│          │     │               │     │  Helper             │
└──────────┘     └───────────────┘     └─────────────────────┘
                                                │
                                                │
                                                ▼
                                       ┌─────────────────────┐
                                       │                     │
                                       │  Balance Service    │
                                       │                     │
                                       └─────────────────────┘
                                                │
                                                │
                                                ▼
                                       ┌─────────────────────┐
                                       │                     │
                                       │  Balance Repository │
                                       │                     │
                                       └─────────────────────┘
```

## 4. Key Implementation Details

### 4.1 Concurrent Data Structures

The application uses thread-safe data structures to handle concurrent access:

```java
private final ConcurrentHashMap<UUID, User> users = new ConcurrentHashMap<>();
private final ConcurrentHashMap<UUID, Group> groups = new ConcurrentHashMap<>();
private final ConcurrentHashMap<UUID, Expense> expenses = new ConcurrentHashMap<>();
private final ConcurrentHashMap<UUID, Transaction> transactions = new ConcurrentHashMap<>();
private final ConcurrentHashMap<String, Double> balances = new ConcurrentHashMap<>();
```

### 4.2 Balance Key Generation

Balances between users are stored using a composite key:

```java
private String getBalanceKey(UUID userId1, UUID userId2) {
    // Ensure consistent key generation regardless of parameter order
    return userId1.compareTo(userId2) < 0 
           ? userId1.toString() + "_" + userId2.toString() 
           : userId2.toString() + "_" + userId1.toString();
}
```

### 4.3 Validation

Input validation is implemented using Jakarta Validation annotations:

```java
public class UserRequestDto {
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
}
```

### 4.4 Exception Handling

Centralized exception handling with GlobalExceptionHandler:

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InvalidSplitException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidSplitException(
            InvalidSplitException ex, WebRequest request) {
        // Exception handling logic
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        // Return formatted error response
    }
    
    // Other exception handlers
}
```

## 5. Testing Strategy

### 5.1 Unit Testing

```java
@Test
public void testCreateUser() {
    // Given
    String name = "John Doe";
    String email = "john@example.com";
    
    // When
    User user = userRepository.createUser(name, email);
    
    // Then
    assertNotNull(user);
    assertEquals(name, user.getName());
    assertEquals(email, user.getEmail());
    assertNotNull(user.getUserId());
}

@Test
public void testAddExpense() {
    // Given
    User payer = userRepository.createUser("John", "john@example.com");
    User user1 = userRepository.createUser("Alice", "alice@example.com");
    User user2 = userRepository.createUser("Bob", "bob@example.com");
    
    List<Split> splits = Arrays.asList(
        new EqualSplit(payer.getUserId()),
        new EqualSplit(user1.getUserId()),
        new EqualSplit(user2.getUserId())
    );
    
    // When
    Expense expense = expenseService.createExpense(
        payer.getUserId(), 300.0, "Dinner", splits, SplitType.EQUAL);
    
    // Then
    assertNotNull(expense);
    assertEquals(300.0, expense.getAmount(), 0.001);
    assertEquals(payer.getUserId(), expense.getPayerId());
    assertEquals(3, expense.getSplits().size());
    
    // Check balances
    assertEquals(100.0, balanceService.getBalance(user1.getUserId(), payer.getUserId()), 0.001);
    assertEquals(100.0, balanceService.getBalance(user2.getUserId(), payer.getUserId()), 0.001);
}
```

### 5.2 Integration Testing

```java
@SpringBootTest
public class SplitWiseIntegrationTest {
    @Autowired
    private SplitWiseController controller;
    
    @Test
    public void testEndToEndExpenseCreation() {
        // Create users
        UserRequestDto user1Dto = new UserRequestDto("John", "john@example.com");
        UserRequestDto user2Dto = new UserRequestDto("Alice", "alice@example.com");
        
        ResponseEntity<UserResponseDto> user1Response = controller.createUser(user1Dto);
        ResponseEntity<UserResponseDto> user2Response = controller.createUser(user2Dto);
        
        UUID user1Id = user1Response.getBody().getUserId();
        UUID user2Id = user2Response.getBody().getUserId();
        
        // Create expense
        ExpenseRequestDto expenseDto = new ExpenseRequestDto();
        expenseDto.setPayerId(user1Id);
        expenseDto.setAmount(200.0);
        expenseDto.setDescription("Lunch");
        expenseDto.setSplitType(SplitType.EQUAL);
        
        List<SplitRequestDto> splitDtos = Arrays.asList(
            new SplitRequestDto(user1Id, null, null),
            new SplitRequestDto(user2Id, null, null)
        );
        expenseDto.setSplits(splitDtos);
        
        controller.addNonGroupExpense(expenseDto);
        
        // Check balance
        ResponseEntity<Double> balanceResponse = controller.getBalance(user2Id, user1Id);
        assertEquals(100.0, balanceResponse.getBody(), 0.001);
    }
}
```

## 6. API Documentation

### 6.1 User Management

```
POST /api/v1/users
Request: {
    "name": "John Doe",
    "email": "john@example.com"
}
Response: {
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "name": "John Doe",
    "email": "john@example.com",
    "createdAt": "2025-05-14T12:00:00"
}
```

### 6.2 Expense Management

```
POST /api/v1/expenses/group
Request: {
    "groupId": "123e4567-e89b-12d3-a456-426614174001",
    "payerId": "123e4567-e89b-12d3-a456-426614174000",
    "amount": 300.0,
    "description": "Dinner",
    "splitType": "EQUAL",
    "splits": [
        {"userId": "123e4567-e89b-12d3-a456-426614174000"},
        {"userId": "123e4567-e89b-12d3-a456-426614174002"},
        {"userId": "123e4567-e89b-12d3-a456-426614174003"}
    ]
}
Response: 201 Created
```

### 6.3 Balance Management

```
GET /api/v1/balances/users/{userId}
Response: {
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "balances": {
        "123e4567-e89b-12d3-a456-426614174002": 100.0,
        "123e4567-e89b-12d3-a456-426614174003": 100.0
    }
}
```
