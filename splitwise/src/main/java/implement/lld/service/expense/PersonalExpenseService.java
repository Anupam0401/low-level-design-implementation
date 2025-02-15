package implement.lld.service.expense;

import implement.lld.exception.InvalidSplitException;
import implement.lld.model.expense.Expense;
import implement.lld.model.expense.ExpenseType;
import implement.lld.model.split.ExactSplit;
import implement.lld.model.split.Split;
import implement.lld.model.split.SplitType;
import implement.lld.repository.ExpenseRepository;
import implement.lld.service.BalanceService;
import lombok.extern.log4j.Log4j2;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Log4j2
public class PersonalExpenseService extends AbstractExpenseService {

    public PersonalExpenseService(BalanceService balanceService, ExpenseRepository expenseRepository) {
        super(balanceService, expenseRepository);
    }

    public void addPersonalExpense(UUID payerId, UUID receiverId, double amount, String description) {
        try {
            List<Split> splits = Collections.singletonList(new ExactSplit(receiverId, amount));
            validateSplits(amount, splits, SplitType.EXACT);

            Expense expense = new Expense(UUID.randomUUID(), payerId, description, amount, splits, ExpenseType.PERSONAL);
            expenseRepository.saveExpense(payerId, description, amount, splits, ExpenseType.PERSONAL, null);

            updateBalances(expense);
            log.info("Added personal expense of ₹{} from user {} to user {}", amount, payerId, receiverId);
        } catch (InvalidSplitException e) {
            log.error("Failed to add personal expense: {}", e.getMessage());
        }
    }
}
