package implement.lld.service.expense;

import implement.lld.model.expense.Expense;
import implement.lld.model.expense.ExpenseType;
import implement.lld.model.split.Split;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ExpenseFactory {
    public static Expense createExpense(
        UUID payerId,
        String description,
        Double amount,
        List<Split> splits,
        ExpenseType expenseType
    ) {
        return switch (expenseType) {
            case GROUP -> new Expense(UUID.randomUUID(), payerId, description, amount, splits, ExpenseType.GROUP);
            case NON_GROUP -> new Expense(UUID.randomUUID(), payerId, description, amount, splits, ExpenseType.NON_GROUP);
            case PERSONAL -> new Expense(UUID.randomUUID(), payerId, description, amount, splits, ExpenseType.PERSONAL);
        };
    }
}
