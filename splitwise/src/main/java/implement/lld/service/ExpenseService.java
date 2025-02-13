package implement.lld.service;

import implement.lld.entities.Expense;
import implement.lld.entities.split.ExactSplit;
import implement.lld.entities.split.PercentSplit;
import implement.lld.entities.split.Split;
import implement.lld.entities.split.SplitType;
import implement.lld.exception.InvalidSplitException;
import lombok.extern.log4j.Log4j2;

import java.util.*;

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

    private void validateExpense(double totalAmount, List<Split> splits, SplitType splitType) {
        if (SplitType.PERCENT.equals(splitType)) {
            double percentSum = splits.stream()
                    .filter(s -> s instanceof PercentSplit)
                    .mapToDouble(s -> ((PercentSplit) s).getPercent())
                    .sum();

            if (percentSum != 100.0) {
                log.error("Invalid split: Sum of percentage splits should be 100%");
                throw new InvalidSplitException("Sum of percentage splits should be 100%");
            }
        }

        double sum = 0.0;
        for (Split split : splits) {
            split.calculateShare(totalAmount, splits);
            sum += split.getAmount();
        }

        if (sum != totalAmount) {
            log.error("Invalid split: Sum of splits (₹{}) does not match total (₹{})", sum, totalAmount);
            throw new InvalidSplitException("Sum of splits should be equal to total amount");
        }
    }

    private void updateBalances(Expense expense) {
        UUID payerId = expense.getPayerId();
        for (Split split : expense.getSplits()) {
            UUID receiverId = split.getUserId();
            balanceService.updateBalance(payerId, receiverId, split.getAmount());
        }
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
            log.error("Failed to add group expense: {}", e.getMessage());
        }
    }

    public void addPersonalExpense(UUID payerId, UUID receiverId, double amount, String description) {
        try {
            List<Split> splits = Collections.singletonList(new ExactSplit(receiverId, amount));
            validateExpense(amount, splits, SplitType.EXACT);

            Expense expense = new Expense(UUID.randomUUID(), payerId, description, amount, splits);
            personalExpenses.add(expense);

            updateBalances(expense);
            log.info("Added personal expense of ₹{} from user {} to user {}", amount, payerId, receiverId);
        } catch (InvalidSplitException e) {
            log.error("Failed to add personal expense: {}", e.getMessage());
        }
    }

}
