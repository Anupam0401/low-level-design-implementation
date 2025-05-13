package implement.lld.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDto {
    private UUID transactionId;
    private UUID senderId;
    private UUID receiverId;
    private double amount;
    private Timestamp createdAt;
}
