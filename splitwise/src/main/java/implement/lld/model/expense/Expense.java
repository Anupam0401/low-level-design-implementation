package implement.lld.model.expense;

import implement.lld.model.split.Split;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
public class Expense {
    private final UUID id;
    private final UUID payerId;
    @Setter
    private String description;
    @Setter
    private Double amount;
    @Setter
    private List<Split> splits;
    private final ExpenseType expenseType;
    @Setter
    private UUID groupId = null;

    public Expense(UUID id, UUID payerId, String description, Double amount, List<Split> splits, ExpenseType expenseType) {
        this.id = id;
        this.payerId = payerId;
        this.description = description;
        this.amount = amount;
        this.splits = splits;
        this.expenseType = expenseType;
    }

}
