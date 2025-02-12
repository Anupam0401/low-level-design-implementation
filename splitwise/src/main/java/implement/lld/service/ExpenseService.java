package implement.lld.service;

import implement.lld.entities.Expense;
import implement.lld.entities.split.Split;
import implement.lld.entities.split.SplitType;
import implement.lld.exception.InvalidSplitException;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Log4j2
public class ExpenseService {
    private final Map<UUID, List<Expense>> groupExpenses;
    private final List<Expense> personalExpenses;
    private final BalanceService balanceService;

    public ExpenseService(BalanceService balanceService) {
        this.groupExpenses = new HashMap<>();
        this.personalExpenses = new ArrayList<>();
        this.balanceService = balanceService;
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
            validateExpense(amount, splits, splitType);

            Expense expense = new Expense(UUID.randomUUID(), payerId, description, amount, splits);
            groupExpenses.computeIfAbsent(groupId, k -> new ArrayList<>()).add(expense);

            updateBalances(expense);
            log.info("Added group expense of ₹{} for group {}", amount, groupId);
        } catch (InvalidSplitException e) {
            log.error("Failed to add group expense for group {}", groupId, e);
        }
    }

    private void validateExpense(double amount, List<Split> splits, SplitType splitType) {

    }

    private void updateBalances(Expense expense) {
        UUID payerId = expense.getPayerId();
        for (Split split : expense.getSplits()) {
            UUID receiverId = split.getUserId();
            balanceService.updateBalance(payerId, receiverId, split.getAmount());
        }
    }

}
