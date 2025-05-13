package implement.lld.repository.interfaces;

import implement.lld.model.expense.Expense;
import implement.lld.model.expense.ExpenseType;
import implement.lld.model.split.Split;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Expense entity operations
 */
public interface IExpenseRepository {
    /**
     * Save an expense to the repository
     * @param payerId the payer user ID
     * @param description the expense description
     * @param amount the expense amount
     * @param splits the list of splits
     * @param expenseType the type of expense
     * @param groupId the group ID (can be null for non-group expenses)
     * @return the saved expense
     */
    Expense saveExpense(
        UUID payerId,
        String description,
        double amount,
        List<Split> splits,
        ExpenseType expenseType,
        UUID groupId
    );
    
    /**
     * Find an expense by ID and type
     * @param expenseId the expense ID
     * @param expenseType the type of expense
     * @return the expense if found, null otherwise
     */
    Expense findExpenseByExpenseIdAndType(UUID expenseId, ExpenseType expenseType);
    
    /**
     * Find an expense by ID
     * @param expenseId the expense ID
     * @return the expense if found, null otherwise
     */
    Expense findExpenseByExpenseId(UUID expenseId);
    
    /**
     * Find expenses by user ID
     * @param payerId the payer user ID
     * @return list of expenses
     */
    List<Expense> findExpensesByUserId(UUID payerId);
    
    /**
     * Find expenses by group ID
     * @param groupId the group ID
     * @return list of expenses
     */
    List<Expense> findExpensesByGroupId(UUID groupId);
    
    /**
     * Update an expense
     * @param expense the expense to update
     * @return the updated expense
     */
    Expense updateExpense(Expense expense);
    
    /**
     * Delete an expense by ID
     * @param expenseId the expense ID
     * @param expenseType the type of expense
     * @return true if deleted, false otherwise
     */
    boolean deleteExpenseById(UUID expenseId, ExpenseType expenseType);
}
