package implement.lld.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    private UUID transactionId;
    private UUID senderId;
    private UUID receiverId;
    private double amount;
    private Timestamp createdAt;

    public Transaction(UUID senderId, UUID receiverId, double amount, Timestamp createdAt) {
        this.transactionId = UUID.randomUUID();
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
        this.createdAt = createdAt;
    }
}
