package implement.lld.repository;

import implement.lld.exception.InvalidExpenseTypeException;
import implement.lld.model.expense.Expense;
import implement.lld.model.expense.ExpenseType;
import implement.lld.model.split.Split;
import implement.lld.service.expense.ExpenseFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ExpenseRepository {
    private final ConcurrentHashMap<UUID, Expense> groupExpenses = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Expense> nonGroupExpenses = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Expense> personalExpenses = new ConcurrentHashMap<>();

    public void saveExpense(
        UUID payerId,
        String description,
        double amount,
        List<Split> splits,
        ExpenseType expenseType,
        UUID groupId
    ) {
        Expense expense = ExpenseFactory.createExpense(payerId, description, amount, splits, expenseType);
        switch (expenseType) {
            case GROUP -> {
                expense.setGroupId(groupId);
                groupExpenses.put(expense.getId(), expense);
            }
            case NON_GROUP -> nonGroupExpenses.put(expense.getId(), expense);
            case PERSONAL -> personalExpenses.put(expense.getId(), expense);
            default -> throw new InvalidExpenseTypeException("Unexpected value of expense type: " + expenseType);
        }
    }

    public Expense findExpenseByExpenseIdAndType(UUID expenseId, ExpenseType expenseType) {
        return switch (expenseType) {
            case GROUP -> groupExpenses.get(expenseId);
            case NON_GROUP -> nonGroupExpenses.get(expenseId);
            case PERSONAL -> personalExpenses.get(expenseId);
        };
    }

    public Expense findExpenseByExpenseId(UUID expenseId) {
        if (groupExpenses.containsKey(expenseId)) {
            return groupExpenses.get(expenseId);
        }
        if (nonGroupExpenses.containsKey(expenseId)) {
            return nonGroupExpenses.get(expenseId);
        }
        if (personalExpenses.containsKey(expenseId)) {
            return personalExpenses.get(expenseId);
        }
        return null;
    }

    public List<Expense> findExpensesByUserId(UUID payerId) {
        List<Expense> expenses = new ArrayList<>();
        for (Expense expense : groupExpenses.values()) {
            if (expense.getPayerId().equals(payerId)) {
                expenses.add(expense);
            }
        }
        for (Expense expense : nonGroupExpenses.values()) {
            if (expense.getPayerId().equals(payerId)) {
                expenses.add(expense);
            }
        }
        for (Expense expense : personalExpenses.values()) {
            if (expense.getPayerId().equals(payerId)) {
                expenses.add(expense);
            }
        }
        return expenses;
    }

    public List<Expense> findExpensesByGroupId(UUID groupId) {
        List<Expense> expenses = new ArrayList<>();
        for (Expense expense : groupExpenses.values()) {
            if (expense.getGroupId().equals(groupId)) {
                expenses.add(expense);
            }
        }
        return expenses;
    }
}
