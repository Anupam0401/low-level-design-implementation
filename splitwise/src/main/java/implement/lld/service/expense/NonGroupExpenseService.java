package implement.lld.service.expense;

import implement.lld.exception.InvalidSplitException;
import implement.lld.model.expense.Expense;
import implement.lld.model.expense.ExpenseType;
import implement.lld.model.split.Split;
import implement.lld.model.split.SplitType;
import implement.lld.repository.impl.ExpenseRepository;
import implement.lld.service.BalanceService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Log4j2
@Service
public class NonGroupExpenseService extends AbstractExpenseService {

    public NonGroupExpenseService(BalanceService balanceService, ExpenseRepository expenseRepository) {
        super(balanceService, expenseRepository);
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

            Expense expense = new Expense(UUID.randomUUID(), payerId, description, amount, splits, ExpenseType.NON_GROUP);
            expenseRepository.saveExpense(payerId, description, amount, splits, ExpenseType.NON_GROUP, null);

            storeExpensesForNonGroupUsers(expense, payerId, splits);

            updateBalances(expense);
            log.info("Added non-group expense of ₹{} for user {} for users {}",
                    amount, payerId, splits.stream().map(Split::getUserId).toList());
        } catch (InvalidSplitException e) {
            log.error("Failed to add non-group expense: {}", e.getMessage());
        }
    }

    private void storeExpensesForNonGroupUsers(Expense expense, UUID payerId, List<Split> splits) {
        expenseRepository.saveExpense(payerId, expense.getDescription(), expense.getAmount(), expense.getSplits(), ExpenseType.NON_GROUP, null);
        for (Split split : splits) {
            UUID receiverId = split.getUserId();
            if (!receiverId.equals(payerId)) {
                expenseRepository.saveExpense(receiverId, expense.getDescription(), expense.getAmount(), expense.getSplits(), ExpenseType.NON_GROUP, null);
            }
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
        Expense expense = findExpense(expenseId);
        if (expense == null) {
            log.error("Expense {} not found for user {} or unauthorized edit attempt", expenseId, payerId);
            throw new IllegalArgumentException("Expense not found or unauthorized edit attempt");
        }

        if(!expense.getPayerId().equals(payerId)) {
            log.error("User {} is not authorized to edit expense {}", payerId, expenseId);
            throw new IllegalArgumentException("User not authorized to edit expense");
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
}
