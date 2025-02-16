package implement.lld.model.split;

import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class PercentSplit extends Split {
    private final double percent;

    public PercentSplit(UUID userId, double percent) {
        super(userId);
        this.percent = percent;
    }

    @Override
    public void calculateShare(Double totalAmount, List<Split> splits) {
        this.amount = totalAmount * (percent / 100.0);
    }
}
