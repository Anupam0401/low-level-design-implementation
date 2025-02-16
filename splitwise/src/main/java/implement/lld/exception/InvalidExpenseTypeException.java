package implement.lld.exception;

import implement.lld.model.expense.ExpenseType;

public class InvalidExpenseTypeException extends RuntimeException {
    public InvalidExpenseTypeException(String message) {
        super(message);
    }
}
