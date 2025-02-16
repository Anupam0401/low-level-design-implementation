package implement.lld.service.expense;

import implement.lld.exception.InvalidSplitException;
import implement.lld.model.expense.Expense;
import implement.lld.model.expense.ExpenseType;
import implement.lld.model.split.Split;
import implement.lld.model.split.SplitType;
import implement.lld.repository.ExpenseRepository;
import implement.lld.service.BalanceService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Log4j2
@Service
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
            if (groupId == null) {
                log.error("Group ID is required for group expenses");
                return;
            }
            validateSplits(amount, splits, splitType);

            Expense expense = new Expense(UUID.randomUUID(), payerId, description, amount, splits, ExpenseType.GROUP);
            expenseRepository.saveExpense(payerId, description, amount, splits, ExpenseType.GROUP, groupId);

            updateBalances(expense);
            log.info("Added group expense of ₹{} for group {}", amount, groupId);
        } catch (InvalidSplitException e) {
            log.error("Failed to add group expense: {}", e.getMessage());
        }
    }

    public List<Expense> findGroupExpenses(UUID groupId) {
        return expenseRepository.findExpensesByGroupId(groupId);
    }

    public Map<UUID, Double> getGroupNetBalance(UUID groupId) {
        List<Expense> groupExpenses = expenseRepository.findExpensesByGroupId(groupId);
        Map<UUID, Double> netBalances = new HashMap<>();

        for (Expense expense : groupExpenses) {
            UUID payer = expense.getPayerId();
            List<Split> splits = expense.getSplits();

            netBalances.putIfAbsent(payer, 0.0);
            netBalances.put(payer, netBalances.get(payer) + expense.getAmount());

            for (Split split : splits) {
                UUID user = split.getUserId();
                double amountOwed = split.getAmount();

                netBalances.putIfAbsent(user, 0.0);
                netBalances.put(user, netBalances.get(user) - amountOwed);
            }
        }

        return netBalances;
    }

}
