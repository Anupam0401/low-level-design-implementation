# Splitwise Low-Level Design (LLD) - Part 1: Core Components

## 1. Domain Model

### 1.1 Class Diagram - Core Entities

```
┌───────────────┐      ┌───────────────┐      ┌───────────────┐
│     User      │      │     Group     │      │    Expense    │
├───────────────┤      ├───────────────┤      ├───────────────┤
│ - userId      │      │ - groupId     │      │ - expenseId   │
│ - name        │      │ - name        │      │ - amount      │
│ - email       │      │ - description │      │ - description │
│ - createdAt   │◄─────┤ - ownerId     │◄─────┤ - payerId     │
│ - updatedAt   │      │ - memberIds   │      │ - expenseType │
└───────────────┘      │ - createdAt   │      │ - splits      │
                       │ - updatedAt   │      │ - createdAt   │
                       └───────────────┘      └───────────────┘
                                                     ▲
                                                     │
                       ┌───────────────┐             │
                       │    Split      │             │
                       ├───────────────┤             │
                       │ - splitId     │◄────────────┘
                       │ - userId      │
                       │ - amount      │
                       │ - percent     │
                       │ - splitType   │
                       └───────────────┘
                              ▲
                              │
              ┌───────────────┼───────────────┐
              │               │               │
    ┌─────────────────┐ ┌─────────────┐ ┌─────────────┐
    │   EqualSplit    │ │ PercentSplit│ │ ExactSplit  │
    ├─────────────────┤ ├─────────────┤ ├─────────────┤
    │                 │ │ - percent   │ │ - amount    │
    └─────────────────┘ └─────────────┘ └─────────────┘


┌───────────────┐      ┌───────────────┐      ┌───────────────┐
│  Transaction  │      │    Balance    │      │DebtSimplifier │
├───────────────┤      ├───────────────┤      ├───────────────┤
│ - transactionId│      │ - userId1    │      │- simplifyDebts│
│ - senderId    │      │ - userId2    │      └───────────────┘
│ - receiverId  │      │ - amount     │
│ - amount      │      └───────────────┘
│ - createdAt   │
└───────────────┘
```

## 2. Repository Layer

### 2.1 Repository Interfaces

```
┌──────────────────────┐      ┌──────────────────────┐      ┌──────────────────────┐
│   IUserRepository    │      │   IGroupRepository   │      │  IExpenseRepository  │
├──────────────────────┤      ├──────────────────────┤      ├──────────────────────┤
│ + createUser()       │      │ + createGroup()      │      │ + createExpense()    │
│ + getUserById()      │      │ + getGroupById()     │      │ + getExpenseById()   │
│ + getAllUsers()      │      │ + getAllGroups()     │      │ + getExpensesByUser()│
│ + updateUser()       │      │ + updateGroup()      │      │ + getExpensesByGroup()│
│ + deleteUser()       │      │ + deleteGroup()      │      │ + updateExpense()    │
└──────────────────────┘      │ + addMember()        │      │ + deleteExpense()    │
                              └──────────────────────┘      └──────────────────────┘


┌──────────────────────┐      ┌──────────────────────┐
│ITransactionRepository│      │  IBalanceRepository  │
├──────────────────────┤      ├──────────────────────┤
│ + createTransaction()│      │ + updateBalance()    │
│ + getTransactionById()│     │ + getBalance()       │
│ + getUserTransactions()│    │ + getUserBalances()  │
└──────────────────────┘      │ + getAllBalances()   │
                              └──────────────────────┘
```

### 2.2 In-Memory Repository Implementations

```
┌────────────────────────┐      ┌────────────────────────┐      ┌────────────────────────┐
│ InMemoryUserRepository │      │InMemoryGroupRepository │      │InMemoryExpenseRepository│
├────────────────────────┤      ├────────────────────────┤      ├────────────────────────┤
│ - users: Map<UUID,User>│      │ - groups: Map<UUID,Group>│    │ - expenses: Map<UUID,Expense>│
│ + createUser()         │      │ + createGroup()        │      │ + createExpense()      │
│ + getUserById()        │      │ + getGroupById()       │      │ + getExpenseById()     │
│ + getAllUsers()        │      │ + getAllGroups()       │      │ + getExpensesByUser()  │
│ + updateUser()         │      │ + updateGroup()        │      │ + getExpensesByGroup() │
│ + deleteUser()         │      │ + deleteGroup()        │      │ + updateExpense()      │
└────────────────────────┘      │ + addMember()          │      │ + deleteExpense()      │
                                └────────────────────────┘      └────────────────────────┘


┌────────────────────────────┐      ┌────────────────────────┐
│InMemoryTransactionRepository│      │InMemoryBalanceRepository│
├────────────────────────────┤      ├────────────────────────┤
│ - transactions: Map<UUID,  │      │ - balances: Map<String,│
│                Transaction>│      │               Double>  │
│ + createTransaction()      │      │ + updateBalance()      │
│ + getTransactionById()     │      │ + getBalance()         │
│ + getUserTransactions()    │      │ + getUserBalances()    │
└────────────────────────────┘      │ + getAllBalances()     │
                                    └────────────────────────┘
```

## 3. Service Layer

### 3.1 Service Classes

```
┌────────────────────┐      ┌────────────────────┐      ┌────────────────────┐
│  SplitWiseService  │      │   BalanceService   │      │ TransactionService │
├────────────────────┤      ├────────────────────┤      ├────────────────────┤
│ - userRepo         │      │ - balanceRepo      │      │ - transactionRepo  │
│ - groupRepo        │      │ + updateBalance()  │      │ + createTransaction()│
│ - expenseRepo      │      │ + getBalance()     │      │ + getTransaction() │
│ - balanceService   │      │ + getUserBalances()│      │ + getUserTransactions()│
│ - transactionService│     │ + getGroupBalances()│     └────────────────────┘
│ + createUser()     │      └────────────────────┘
│ + getUser()        │
│ + getAllUsers()    │      ┌────────────────────┐
│ + createGroup()    │      │DebtSimplificationHelper│
│ + getGroup()       │      ├────────────────────┤
│ + addMemberToGroup()│     │ - balanceService   │
│ + addPersonalExpense()│   │ - groupExpenseService│
│ + addNonGroupExpense()│   │ + simplifyDebts()  │
│ + addGroupExpense()│      └────────────────────┘
│ + getExpensesByUserId()│
│ + getExpensesByGroupId()│
│ + getBalance()     │
│ + getUserBalances()│
│ + getGroupNetBalances()│
│ + performTransaction()│
│ + getUserTransactions()│
│ + getTransaction() │
└────────────────────┘
```

### 3.2 Expense Service Hierarchy

```
┌────────────────────────┐
│  AbstractExpenseService │
├────────────────────────┤
│ # expenseRepository    │
│ # balanceService       │
│ + createExpense()      │
│ + validateSplits()     │
│ + processExpense()     │
└────────────────────────┘
           ▲
           │
           │
┌──────────┴───────────┬────────────────────┐
│                      │                    │
│                      │                    │
┌────────────────┐ ┌───────────────┐ ┌─────────────────┐
│GroupExpenseService│ │PersonalExpenseService│ │NonGroupExpenseService│
├────────────────┤ ├───────────────┤ ├─────────────────┤
│ - groupRepo    │ │               │ │                 │
│ + createExpense()│ │ + createExpense()│ │ + createExpense()│
│ + getGroupNetBalance()│               │ │                 │
└────────────────┘ └───────────────┘ └─────────────────┘
```

## 4. Controller Layer

```
┌────────────────────────┐      ┌────────────────────────┐
│   SplitWiseController  │      │  HealthCheckController │
├────────────────────────┤      ├────────────────────────┤
│ - splitWiseService     │      │ + ping()              │
│ - userMapper           │      │ + status()            │
│ - groupMapper          │      │ + health()            │
│ - expenseMapper        │      └────────────────────────┘
│ - transactionMapper    │
│ - balanceMapper        │
│ - debtSimplificationHelper│
│ + createUser()         │
│ + getUser()            │
│ + getAllUsers()        │
│ + createGroup()        │
│ + getGroup()           │
│ + addMemberToGroup()   │
│ + addPersonalExpense() │
│ + addNonGroupExpense() │
│ + addGroupExpense()    │
│ + getExpensesByUserId()│
│ + getExpensesByGroupId()│
│ + getBalance()         │
│ + getUserBalances()    │
│ + getGroupNetBalances()│
│ + getSimplifiedGroupDebts()│
│ + getSimplifiedUserDebts()│
│ + performTransaction() │
│ + getUserTransactions()│
│ + getTransaction()     │
└────────────────────────┘
```

## 5. DTO Layer

### 5.1 Request DTOs

```
┌────────────────────┐      ┌────────────────────┐      ┌────────────────────┐
│   UserRequestDto   │      │   GroupRequestDto  │      │  ExpenseRequestDto │
├────────────────────┤      ├────────────────────┤      ├────────────────────┤
│ - name             │      │ - name             │      │ - payerId          │
│ - email            │      │ - description      │      │ - amount           │
└────────────────────┘      │ - ownerId          │      │ - description      │
                            │ - memberIds        │      │ - groupId          │
                            └────────────────────┘      │ - expenseType      │
                                                        │ - splitType        │
┌────────────────────┐                                  │ - splits           │
│   SplitRequestDto  │                                  └────────────────────┘
├────────────────────┤
│ - userId           │
│ - amount           │
│ - percent          │
└────────────────────┘
```

### 5.2 Response DTOs

```
┌────────────────────┐      ┌────────────────────┐      ┌────────────────────┐
│   UserResponseDto  │      │  GroupResponseDto  │      │ ExpenseResponseDto │
├────────────────────┤      ├────────────────────┤      ├────────────────────┤
│ - userId           │      │ - groupId          │      │ - expenseId        │
│ - name             │      │ - name             │      │ - amount           │
│ - email            │      │ - description      │      │ - description      │
│ - createdAt        │      │ - ownerId          │      │ - payerId          │
└────────────────────┘      │ - memberIds        │      │ - expenseType      │
                            │ - createdAt        │      │ - splits           │
                            └────────────────────┘      │ - createdAt        │
                                                        └────────────────────┘

┌────────────────────┐      ┌────────────────────┐      ┌────────────────────┐
│   SplitResponseDto │      │TransactionResponseDto│     │BalanceResponseDto │
├────────────────────┤      ├────────────────────┤      ├────────────────────┤
│ - splitId          │      │ - transactionId    │      │ - userId           │
│ - userId           │      │ - senderId         │      │ - balances         │
│ - amount           │      │ - receiverId       │      └────────────────────┘
│ - percent          │      │ - amount           │
│ - splitType        │      │ - createdAt        │
└────────────────────┘      └────────────────────┘

┌────────────────────┐
│  ErrorResponseDto  │
├────────────────────┤
│ - timestamp        │
│ - status           │
│ - error            │
│ - message          │
│ - path             │
└────────────────────┘
```

## 6. Mapper Classes

```
┌────────────────────┐      ┌────────────────────┐      ┌────────────────────┐
│     UserMapper     │      │    GroupMapper     │      │   ExpenseMapper    │
├────────────────────┤      ├────────────────────┤      ├────────────────────┤
│ + toDto()          │      │ + toDto()          │      │ + toDto()          │
│ + toDtoList()      │      │ + toDtoList()      │      │ + toDtoList()      │
└────────────────────┘      └────────────────────┘      │ + toSplitEntities()│
                                                        └────────────────────┘

┌────────────────────┐      ┌────────────────────┐
│  TransactionMapper │      │   BalanceMapper    │
├────────────────────┤      ├────────────────────┤
│ + toDto()          │      │ + toDto()          │
│ + toDtoList()      │      └────────────────────┘
└────────────────────┘
```

## 7. Exception Handling

```
┌────────────────────────┐
│ GlobalExceptionHandler │
├────────────────────────┤
│ + handleInvalidSplitException()      │
│ + handleInvalidExpenseTypeException()│
│ + handleIllegalArgumentException()   │
│ + handleValidationExceptions()       │
│ + handleConstraintViolationException()│
│ + handleGlobalException()            │
└────────────────────────┘

┌────────────────────┐      ┌────────────────────┐
│InvalidSplitException│      │InvalidExpenseTypeException│
├────────────────────┤      ├────────────────────┤
│ + message          │      │ + message          │
└────────────────────┘      └────────────────────┘
```

## 8. Configuration

```
┌────────────────────┐
│     AppConfig      │
├────────────────────┤
│ + userRepository() │
│ + groupRepository()│
│ + transactionRepository()│
│ + balanceRepository()│
└────────────────────┘
```
