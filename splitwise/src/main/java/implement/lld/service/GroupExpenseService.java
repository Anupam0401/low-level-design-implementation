package implement.lld.service;

import implement.lld.model.expense.Expense;
import implement.lld.model.split.ExactSplit;
import implement.lld.model.split.Split;
import implement.lld.model.split.SplitType;
import implement.lld.exception.InvalidSplitException;
import implement.lld.repository.ExpenseRepository;
import lombok.extern.log4j.Log4j2;

import java.util.*;

@Log4j2
public class GroupExpenseService extends AbstractExpenseService {

    public GroupExpenseService(BalanceService balanceService, ExpenseRepository expenseRepository) {
        super(balanceService, expenseRepository);
    }

    public void addGroupExpense(
        UUID groupId,
        UUID payerId,
        double amount,
        String description,
        List<Split> splits,
        SplitType splitType
    ) {
        try {
            validateSplits(amount, splits, splitType);

            Expense expense = new Expense(UUID.randomUUID(), payerId, description, amount, splits);
            groupExpenses.computeIfAbsent(groupId, k -> new ArrayList<>()).add(expense);

            updateBalances(expense);
            log.info("Added group expense of ₹{} for group {}", amount, groupId);
        } catch (InvalidSplitException e) {
            log.error("Failed to add group expense: {}", e.getMessage());
        }
    }

    public void addPersonalExpense(UUID payerId, UUID receiverId, double amount, String description) {
        try {
            List<Split> splits = Collections.singletonList(new ExactSplit(receiverId, amount));
            validateSplits(amount, splits, SplitType.EXACT);

            Expense expense = new Expense(UUID.randomUUID(), payerId, description, amount, splits);
            personalExpenses.add(expense);

            updateBalances(expense);
            log.info("Added personal expense of ₹{} from user {} to user {}", amount, payerId, receiverId);
        } catch (InvalidSplitException e) {
            log.error("Failed to add personal expense: {}", e.getMessage());
        }
    }

    public void addNonGroupExpense(
        UUID payerId,
        double amount,
        String description,
        List<Split> splits,
        SplitType splitType
    ) {
        try {
            validateSplits(amount, splits, splitType);

            Expense expense = new Expense(UUID.randomUUID(), payerId, description, amount, splits);
            userNonGroupExpenses.computeIfAbsent(payerId, k -> new ArrayList<>()).add(expense);

            storeExpensesForNonGroupUsers(expense, payerId, splits);

            updateBalances(expense);
            log.info("Added non-group expense of ₹{} for user {} for users {}",
                    amount, payerId, splits.stream().map(Split::getUserId).toList());
        } catch (InvalidSplitException e) {
            log.error("Failed to add non-group expense: {}", e.getMessage());
        }
    }

    private void storeExpensesForNonGroupUsers(Expense expense, UUID payerId, List<Split> splits) {
        userNonGroupExpenses.computeIfAbsent(payerId, k -> new ArrayList<>()).add(expense);
        for (Split split : splits) {
            UUID receiverId = split.getUserId();
            userNonGroupExpenses.computeIfAbsent(receiverId, k -> new ArrayList<>()).add(expense);
        }
    }

    /**
     * Users should only edit expenses they created (payer-only permission).
     * If amount or participants change, we must recalculate splits and update balances accordingly.
     */
    public void editNonGroupExpenses(
        UUID expenseId,
        UUID payerId,
        double newAmount,
        String newDescription,
        List<Split> newSplits,
        SplitType splitType
    ) {
        Expense expense = findExpense(payerId, expenseId);
        if (expense == null) {
            log.error("Expense {} not found for user {} or unauthorized edit attempt", expenseId, payerId);
            throw new IllegalArgumentException("Expense not found or unauthorized edit attempt");
        }

        rollbackExpense(expense);

        try {
            validateSplits(newAmount, newSplits, splitType);

            expense.setAmount(newAmount);
            expense.setSplits(newSplits);
            expense.setDescription(newDescription);

            storeExpensesForNonGroupUsers(expense, payerId, newSplits);
            updateBalances(expense);

            log.info("Edited non-group expense {} successfully by user {}", expenseId, payerId);
        } catch (InvalidSplitException e) {
            log.error("Failed to edit non-group expense: {}", e.getMessage());
        }
    }

    private Expense findExpense(UUID userId, UUID expenseId) {
        List<Expense> expenses = userNonGroupExpenses.getOrDefault(userId, new ArrayList<>());
        return expenses.stream()
                .filter(e -> e.getId().equals(expenseId))
                .findFirst()
                .orElse(null);
    }
}
