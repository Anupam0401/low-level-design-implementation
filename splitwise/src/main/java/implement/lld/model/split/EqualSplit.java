package implement.lld.model.split;

import java.util.List;
import java.util.UUID;

public class EqualSplit extends Split {
    public EqualSplit(UUID userId) {
        super(userId);
    }

    @Override
    public void calculateShare(Double totalAmount, List<Split> splits) {
        this.amount = totalAmount / splits.size();
    }
}
