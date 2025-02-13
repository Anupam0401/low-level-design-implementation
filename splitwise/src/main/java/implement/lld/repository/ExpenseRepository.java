package implement.lld.repository;

import implement.lld.model.expense.Expense;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ExpenseRepository {
    private final ConcurrentHashMap<UUID, Expense> groupExpenses = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Expense> nonGroupExpenses = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Expense> personalExpenses = new ConcurrentHashMap<>();

    public void saveExpense()

}
