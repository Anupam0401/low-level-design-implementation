package implement.lld.service;

import implement.lld.exception.InvalidSplitException;
import implement.lld.model.expense.Expense;
import implement.lld.model.split.PercentSplit;
import implement.lld.model.split.Split;
import implement.lld.model.split.SplitType;
import implement.lld.repository.ExpenseRepository;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Log4j2
public abstract class AbstractExpenseService {
    private final BalanceService balanceService;
    private final ExpenseRepository expenseRepository;

    public AbstractExpenseService(BalanceService balanceService, ExpenseRepository expenseRepository) {
        this.balanceService = balanceService;
        this.expenseRepository = expenseRepository;
    }

    protected void validateSplits(double totalAmount, List<Split> splits, SplitType splitType) {
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

    protected void updateBalances(Expense expense) {
        UUID payerId = expense.getPayerId();
        for (Split split : expense.getSplits()) {
            UUID receiverId = split.getUserId();
            balanceService.updateBalance(payerId, receiverId, split.getAmount());
        }
    }

    protected Expense findExpense(UUID userId, UUID expenseId) {
        List<Expense> expenses = expenseRepository.findExpenses(userId, expenseId);
    }

    protected void rollbackExpense(Expense expense) {
        UUID payerId = expense.getPayerId();
        for (Split split : expense.getSplits()) {
            UUID receiverId = split.getUserId();
            balanceService.updateBalance(payerId, receiverId, -split.getAmount());
        }
    }
}
