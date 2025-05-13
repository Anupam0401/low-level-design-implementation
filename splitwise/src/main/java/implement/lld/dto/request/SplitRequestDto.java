package implement.lld.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SplitRequestDto {
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    // For EXACT split
    private Double amount;
    
    // For PERCENT split
    private Double percent;
}
