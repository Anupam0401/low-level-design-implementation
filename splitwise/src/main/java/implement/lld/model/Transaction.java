package implement.lld.model;

import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Builder
public class Transaction {
    private final UUID transactionId;
    private final UUID senderId;
    private final UUID receiverId;
    private final double amount;
    private final Timestamp createdAt;

    public Transaction(UUID senderId, UUID receiverId, double amount, Timestamp createdAt) {
        this.transactionId = UUID.randomUUID();
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
        this.createdAt = createdAt;
    }
}
