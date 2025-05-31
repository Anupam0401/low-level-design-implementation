# Splitwise Low-Level Design - Part 3: Sequence Diagrams

This document provides sequence diagrams for key operations in the Splitwise application, illustrating the interactions between components.

## 1. User Creation Sequence

```
┌──────┐      ┌───────────────────┐      ┌────────────────┐      ┌─────────────────┐      ┌──────────────────┐
│Client│      │SplitWiseController│      │SplitWiseService│      │UserRepository   │      │UserMapper        │
└──┬───┘      └────────┬──────────┘      └───────┬────────┘      └────────┬────────┘      └────────┬─────────┘
   │                   │                         │                        │                        │
   │ POST /users       │                         │                        │                        │
   │ (UserRequestDto)  │                         │                        │                        │
   │──────────────────>│                         │                        │                        │
   │                   │                         │                        │                        │
   │                   │ createUser(name, email) │                        │                        │
   │                   │────────────────────────>│                        │                        │
   │                   │                         │                        │                        │
   │                   │                         │ createUser(name, email)│                        │
   │                   │                         │───────────────────────>│                        │
   │                   │                         │                        │                        │
   │                   │                         │                        │ Create User            │
   │                   │                         │                        │─────────────┐          │
   │                   │                         │                        │             │          │
   │                   │                         │                        │<────────────┘          │
   │                   │                         │                        │                        │
   │                   │                         │ User                   │                        │
   │                   │                         │<───────────────────────│                        │
   │                   │                         │                        │                        │
   │                   │ User                    │                        │                        │
   │                   │<────────────────────────│                        │                        │
   │                   │                         │                        │                        │
   │                   │ toDto(user)             │                        │                        │
   │                   │───────────────────────────────────────────────────────────────>│          │
   │                   │                         │                        │              │          │
   │                   │                         │                        │              │ Map User to DTO
   │                   │                         │                        │              │──────────┐
   │                   │                         │                        │              │          │
   │                   │                         │                        │              │<─────────┘
   │                   │                         │                        │              │          │
   │                   │ UserResponseDto         │                        │              │          │
   │                   │<──────────────────────────────────────────────────────────────│          │
   │                   │                         │                        │                        │
   │ 201 Created      │                         │                        │                        │
   │ (UserResponseDto) │                         │                        │                        │
   │<─────────────────│                         │                        │                        │
   │                   │                         │                        │                        │
```

## 2. Adding a Group Expense Sequence

```
┌──────┐      ┌────────────────┐      ┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│Client│      │SplitWiseController│      │SplitWiseService │      │GroupExpenseService│      │ExpenseRepository│
└──┬───┘      └────────┬─────────┘      └────────┬────────┘      └────────┬────────┘      └────────┬────────┘
   │                   │                         │                        │                        │
   │ POST /expenses/group                        │                        │                        │
   │ (ExpenseRequestDto)│                         │                        │                        │
   │──────────────────>│                         │                        │                        │
   │                   │                         │                        │                        │
   │                   │ addGroupExpense()       │                        │                        │
   │                   │────────────────────────>│                        │                        │
   │                   │                         │                        │                        │
   │                   │                         │ createExpense()        │                        │
   │                   │                         │───────────────────────>│                        │
   │                   │                         │                        │                        │
   │                   │                         │                        │ Validate inputs        │
   │                   │                         │                        │─────────────┐          │
   │                   │                         │                        │             │          │
   │                   │                         │                        │<────────────┘          │
   │                   │                         │                        │                        │
   │                   │                         │                        │ createExpenseEntity()  │
   │                   │                         │                        │─────────────┐          │
   │                   │                         │                        │             │          │
   │                   │                         │                        │<────────────┘          │
   │                   │                         │                        │                        │
   │                   │                         │                        │ createExpense()        │
   │                   │                         │                        │───────────────────────>│
   │                   │                         │                        │                        │
   │                   │                         │                        │                        │
   │                   │                         │                        │ Expense                │
   │                   │                         │                        │<───────────────────────│
   │                   │                         │                        │                        │
   │                   │                         │                        │ Update balances        │
   │                   │                         │                        │─────────────┐          │
   │                   │                         │                        │             │          │
   │                   │                         │                        │<────────────┘          │
   │                   │                         │                        │                        │
   │                   │                         │ Expense                │                        │
   │                   │                         │<───────────────────────│                        │
   │                   │                         │                        │                        │
   │                   │ Expense                 │                        │                        │
   │                   │<────────────────────────│                        │                        │
   │                   │                         │                        │                        │
   │ 201 Created       │                         │                        │                        │
   │<─────────────────│                         │                        │                        │
   │                   │                         │                        │                        │
```

## 3. Getting User Balances Sequence

```
┌──────┐      ┌────────────────┐      ┌─────────────────┐      ┌─────────────────┐      ┌──────────────────┐
│Client│      │SplitWiseController│      │SplitWiseService │      │BalanceRepository│      │BalanceMapper    │
└──┬───┘      └────────┬─────────┘      └────────┬────────┘      └────────┬────────┘      └────────┬─────────┘
   │                   │                         │                        │                        │
   │ GET /balances/users/{userId}                │                        │                        │
   │──────────────────>│                         │                        │                        │
   │                   │                         │                        │                        │
   │                   │ getUserBalances(userId) │                        │                        │
   │                   │────────────────────────>│                        │                        │
   │                   │                         │                        │                        │
   │                   │                         │ getUserBalances(userId)│                        │
   │                   │                         │───────────────────────>│                        │
   │                   │                         │                        │                        │
   │                   │                         │                        │ Retrieve balances      │
   │                   │                         │                        │─────────────┐          │
   │                   │                         │                        │             │          │
   │                   │                         │                        │<────────────┘          │
   │                   │                         │                        │                        │
   │                   │                         │ Map<UUID, Double>      │                        │
   │                   │                         │<───────────────────────│                        │
   │                   │                         │                        │                        │
   │                   │ Map<UUID, Double>       │                        │                        │
   │                   │<────────────────────────│                        │                        │
   │                   │                         │                        │                        │
   │                   │ toDto(userId, balances) │                        │                        │
   │                   │───────────────────────────────────────────────────────────────>│          │
   │                   │                         │                        │              │          │
   │                   │                         │                        │              │ Map to DTO
   │                   │                         │                        │              │──────────┐
   │                   │                         │                        │              │          │
   │                   │                         │                        │              │<─────────┘
   │                   │                         │                        │              │          │
   │                   │ BalanceResponseDto      │                        │              │          │
   │                   │<──────────────────────────────────────────────────────────────│          │
   │                   │                         │                        │                        │
   │ 200 OK            │                         │                        │                        │
   │ (BalanceResponseDto)                        │                        │                        │
   │<─────────────────│                         │                        │                        │
   │                   │                         │                        │                        │
```

## 4. Debt Simplification Sequence

```
┌──────┐      ┌────────────────┐      ┌─────────────────────┐      ┌─────────────────┐
│Client│      │SplitWiseController│      │DebtSimplificationHelper│      │SplitWiseService │
└──┬───┘      └────────┬─────────┘      └─────────┬───────────┘      └────────┬────────┘
   │                   │                          │                           │
   │ GET /balances/groups/{groupId}/simplify      │                           │
   │──────────────────>│                          │                           │
   │                   │                          │                           │
   │                   │ getSimplifiedGroupDebts(groupId)                     │
   │                   │─────────────────────────>│                           │
   │                   │                          │                           │
   │                   │                          │ getGroupNetBalances(groupId)
   │                   │                          │────────────────────────────>
   │                   │                          │                           │
   │                   │                          │                           │ Retrieve balances
   │                   │                          │                           │────────────┐
   │                   │                          │                           │            │
   │                   │                          │                           │<───────────┘
   │                   │                          │                           │
   │                   │                          │ Map<UUID, Double>         │
   │                   │                          │<────────────────────────────
   │                   │                          │                           │
   │                   │                          │ simplifyDebts(balances)   │
   │                   │                          │──────────────┐            │
   │                   │                          │              │            │
   │                   │                          │<─────────────┘            │
   │                   │                          │                           │
   │                   │ List<SimplifiedTransaction>                          │
   │                   │<─────────────────────────│                           │
   │                   │                          │                           │
   │ 200 OK            │                          │                           │
   │ (List<SimplifiedTransaction>)                │                           │
   │<─────────────────│                          │                           │
   │                   │                          │                           │
```

## 5. Performing a Transaction Sequence

```
┌──────┐      ┌────────────────┐      ┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│Client│      │SplitWiseController│      │SplitWiseService │      │TransactionService│      │BalanceService   │
└──┬───┘      └────────┬─────────┘      └────────┬────────┘      └────────┬────────┘      └────────┬────────┘
   │                   │                         │                        │                        │
   │ POST /transactions│                         │                        │                        │
   │ (senderId, receiverId, amount)              │                        │                        │
   │──────────────────>│                         │                        │                        │
   │                   │                         │                        │                        │
   │                   │ performTransaction()    │                        │                        │
   │                   │────────────────────────>│                        │                        │
   │                   │                         │                        │                        │
   │                   │                         │ createTransaction()    │                        │
   │                   │                         │───────────────────────>│                        │
   │                   │                         │                        │                        │
   │                   │                         │                        │ Create transaction     │
   │                   │                         │                        │─────────────┐          │
   │                   │                         │                        │             │          │
   │                   │                         │                        │<────────────┘          │
   │                   │                         │                        │                        │
   │                   │                         │ Transaction            │                        │
   │                   │                         │<───────────────────────│                        │
   │                   │                         │                        │                        │
   │                   │                         │ updateBalance()        │                        │
   │                   │                         │───────────────────────────────────────────────>│
   │                   │                         │                        │                        │
   │                   │                         │                        │                        │ Update balances
   │                   │                         │                        │                        │─────────────┐
   │                   │                         │                        │                        │             │
   │                   │                         │                        │                        │<────────────┘
   │                   │                         │                        │                        │
   │                   │                         │ Success                │                        │
   │                   │                         │<──────────────────────────────────────────────│
   │                   │                         │                        │                        │
   │                   │ Boolean                 │                        │                        │
   │                   │<────────────────────────│                        │                        │
   │                   │                         │                        │                        │
   │ 200 OK            │                         │                        │                        │
   │ (Boolean)         │                         │                        │                        │
   │<─────────────────│                         │                        │                        │
   │                   │                         │                        │                        │
```

## 6. Error Handling Sequence

```
┌──────┐      ┌────────────────┐      ┌───────────────────────┐
│Client│      │SplitWiseController│      │GlobalExceptionHandler│
└──┬───┘      └────────┬─────────┘      └───────────┬─────────┘
   │                   │                            │
   │ POST /expenses/group                           │
   │ (Invalid data)    │                            │
   │──────────────────>│                            │
   │                   │                            │
   │                   │ Validation error           │
   │                   │ or Exception               │
   │                   │────────────────────────────>
   │                   │                            │
   │                   │                            │ Handle exception
   │                   │                            │─────────────┐
   │                   │                            │             │
   │                   │                            │<────────────┘
   │                   │                            │
   │                   │ ErrorResponseDto           │
   │                   │<────────────────────────────
   │                   │                            │
   │ 400 Bad Request   │                            │
   │ (ErrorResponseDto)│                            │
   │<─────────────────│                            │
   │                   │                            │
```

## 7. Split Validation Sequence

```
┌─────────────────┐      ┌─────────────────┐
│AbstractExpenseService│      │SplitValidator    │
└────────┬────────┘      └────────┬────────┘
         │                        │
         │ validateSplits()       │
         │───────────────────────>│
         │                        │
         │                        │ Check split type
         │                        │─────────────┐
         │                        │             │
         │                        │<────────────┘
         │                        │
         │                        │ Validate based on type
         │                        │─────────────┐
         │                        │             │
         │                        │<────────────┘
         │                        │
         │ Valid/Invalid          │
         │<───────────────────────│
         │                        │
```

These sequence diagrams illustrate the key interactions between components in the Splitwise application for major operations. They provide a clear view of how data flows through the system and how different components collaborate to fulfill user requests.
