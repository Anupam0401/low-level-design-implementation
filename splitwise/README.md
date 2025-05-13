# Designing Splitwise

## Requirements
1. The system should allow users to create accounts and manage their profile information.
2. Users should be able to create groups and add other users to the groups.
3. Users should be able to add expenses within a group, specifying the amount, description, and participants.
4. The system should automatically split the expenses among the participants based on their share.
5. Users should be able to view their individual balances with other users and settle up the balances.
6. The system should support different split methods, such as equal split, percentage split, and exact amounts.
7. Users should be able to view their transaction history and group expenses.
8. The system should handle concurrent transactions and ensure data consistency.

## Implementation Overview

### Architecture
The application follows a layered architecture with:
- **Model Layer**: Domain entities representing the core business objects
- **Repository Layer**: Data access abstraction with interfaces and implementations
- **Service Layer**: Business logic implementation
- **Controller Layer**: REST API endpoints for client interaction
- **DTO Layer**: Data Transfer Objects for API request/response

### Design Patterns Used
1. **Repository Pattern**: Abstracts data access logic, enabling easy switching between data sources
2. **Factory Pattern**: Used for creating different types of expenses and splits
3. **Strategy Pattern**: Different split calculation strategies (Equal, Exact, Percent)
4. **Dependency Injection**: Services depend on repository interfaces, not implementations
5. **Builder Pattern**: Used for complex object construction (e.g., Transaction)

### SOLID Principles Implementation
1. **Single Responsibility**: Each class has a single reason to change
   - Repository classes handle only data access
   - Service classes contain only business logic
   - Controllers handle only HTTP request/response
   
2. **Open/Closed**: Classes are open for extension but closed for modification
   - Split hierarchy allows adding new split types without modifying existing code
   - Expense types can be extended without changing core logic
   
3. **Liskov Substitution**: Subtypes can be substituted for their base types
   - All Split implementations work correctly through the base class
   - Repository implementations are interchangeable
   
4. **Interface Segregation**: Clients don't depend on interfaces they don't use
   - Repository interfaces are specific to entity types
   - Service interfaces expose only relevant methods
   
5. **Dependency Inversion**: High-level modules depend on abstractions
   - Services depend on repository interfaces, not concrete implementations
   - Controllers depend on service interfaces

## Classes, Interfaces and Enumerations

### Core Domain Models
1. **User**: Represents a user with ID, name, and email
2. **Group**: Contains members and group metadata
3. **Expense**: Abstract base for different expense types
4. **Split**: Abstract base for different split strategies (Equal, Exact, Percent)
5. **Transaction**: Records money movement between users
6. **Balance**: Tracks what users owe each other

### Repository Layer
1. **IUserRepository**: Interface for user data operations
2. **IGroupRepository**: Interface for group data operations
3. **IExpenseRepository**: Interface for expense data operations
4. **ITransactionRepository**: Interface for transaction data operations
5. **IBalanceRepository**: Interface for balance data operations

### In-Memory Implementations
1. **InMemoryUserRepository**: Thread-safe user storage using ConcurrentHashMap
2. **InMemoryGroupRepository**: Thread-safe group storage
3. **InMemoryExpenseRepository**: Stores different expense types
4. **InMemoryTransactionRepository**: Records all transactions
5. **InMemoryBalanceRepository**: Manages user balances

### Service Layer
1. **SplitWiseService**: Main orchestration service
2. **BalanceService**: Handles balance calculations and updates
3. **TransactionService**: Manages money transfers between users
4. **ExpenseService**: Abstract base for expense operations
   - **PersonalExpenseService**: Handles personal expenses
   - **GroupExpenseService**: Handles group expenses
   - **NonGroupExpenseService**: Handles expenses between users without a group

### Controller Layer
**SplitWiseController**: REST API endpoints for all operations:
- User management
- Group management
- Expense management
- Balance queries
- Transaction operations

### DTO Layer
1. **Request DTOs**: Validate and transfer client input
2. **Response DTOs**: Format data for client consumption
3. **Mappers**: Convert between domain models and DTOs

## Concurrency Handling
- **Thread-safe collections**: ConcurrentHashMap and CopyOnWriteArrayList
- **Lock-based synchronization**: ReentrantLock for transaction operations
- **Atomic operations**: Using compute and computeIfAbsent for thread safety

## Database Migration Strategy

The application is currently using in-memory storage with a clean repository abstraction layer, making it easy to migrate to a real database system in the future. The migration strategy involves:

### 1. Database Schema Design
- Create tables corresponding to domain entities
- Define relationships and constraints
- Set up indexing for performance

### 2. JPA Entity Mapping
- Add JPA annotations to domain models
- Create entity classes if domain models need to remain clean
- Define relationships between entities

### 3. Spring Data Repository Implementation
- Create JPA repository interfaces extending Spring Data repositories
- Implement custom repository methods as needed
- Ensure all repository interfaces are implemented

### 4. Transaction Management
- Add @Transactional annotations to service methods
- Configure transaction boundaries
- Implement proper exception handling

### 5. Connection Pool Configuration
- Set up HikariCP or similar connection pool
- Configure database connection properties
- Optimize pool settings for performance

### 6. Data Migration
- Create scripts to migrate in-memory data to the database
- Implement data validation and verification
- Plan for zero-downtime migration if needed

### 7. Testing
- Create integration tests with the real database
- Verify all functionality works with the new implementation
- Benchmark performance and optimize as needed

## API Documentation

The REST API provides the following endpoints:

### User Management
- `POST /api/v1/users`: Create a new user
- `GET /api/v1/users/{userId}`: Get user details
- `GET /api/v1/users`: Get all users

### Group Management
- `POST /api/v1/groups`: Create a new group
- `GET /api/v1/groups/{groupId}`: Get group details
- `POST /api/v1/groups/{groupId}/members/{userId}`: Add a member to a group

### Expense Management
- `POST /api/v1/expenses/personal`: Add a personal expense
- `POST /api/v1/expenses/non-group`: Add a non-group expense
- `POST /api/v1/expenses/group`: Add a group expense
- `GET /api/v1/expenses/users/{userId}`: Get expenses for a user
- `GET /api/v1/expenses/groups/{groupId}`: Get expenses for a group

### Balance Management
- `GET /api/v1/balances/users/{userId1}/users/{userId2}`: Get balance between two users
- `GET /api/v1/balances/users/{userId}`: Get all balances for a user
- `GET /api/v1/balances/groups/{groupId}`: Get net balances for a group

### Transaction Management
- `POST /api/v1/transactions`: Perform a transaction between users
- `GET /api/v1/transactions/users/{userId}`: Get transactions for a user
- `GET /api/v1/transactions/{transactionId}`: Get transaction details