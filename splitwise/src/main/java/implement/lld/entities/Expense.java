package implement.lld.entities;

import implement.lld.entities.split.Split;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class Expense {
    private final UUID id;
    private final UUID payerId;
    private final String description;
    private final Double amount;
    private final List<Split> splits;

    public Expense(UUID id, UUID payerId, String description, Double amount, List<Split> splits) {
        this.id = id;
        this.payerId = payerId;
        this.description = description;
        this.amount = amount;
        this.splits = splits;
    }

}
