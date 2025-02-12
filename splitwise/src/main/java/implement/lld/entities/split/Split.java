package implement.lld.entities.split;

import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public abstract class Split {
    protected final UUID userId;
    protected Double amount;

    public Split(UUID userId) {
        this.userId = userId;
    }

    public abstract void calculateShare(Double totalAmount, List<Split> splits);
}
