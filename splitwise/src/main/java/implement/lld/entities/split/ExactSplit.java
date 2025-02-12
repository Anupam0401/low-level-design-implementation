package implement.lld.entities.split;

import java.util.List;
import java.util.UUID;

public class ExactSplit extends Split {
    public ExactSplit(UUID userId, Double amount) {
        super(userId);
        this.amount = amount;
    }

    @Override
    public void calculateShare(Double totalAmount, List<Split> splits) {
        // Do nothing
    }
}
