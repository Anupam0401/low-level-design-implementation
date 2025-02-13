package implement.lld.service;

import implement.lld.exception.InvalidExpenseTypeException;
import implement.lld.model.expense.Expense;
import implement.lld.model.expense.ExpenseType;
import implement.lld.model.split.Split;

import java.util.List;
import java.util.UUID;

import static java.util.List.of;

public class ExpenseFactory {
    public static Expense createExpense(UUID payerId, String description, Double amount, List<Split> splits, ExpenseType expenseType, UUID groupId) {
        return switch (expenseType) {
            case GROUP -> new Expense(groupId, payerId, description, amount, splits, ExpenseType.GROUP);
            case PERSONAL, NON_GROUP ->
                    new Expense(UUID.randomUUID(), payerId, description, amount, of(splits.get(0)), ExpenseType.PERSONAL);
            default -> new InvalidExpenseTypeException("Invalid expense type");
        };
    }
}
