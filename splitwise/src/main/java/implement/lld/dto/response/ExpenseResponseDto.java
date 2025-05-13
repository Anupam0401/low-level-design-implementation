package implement.lld.dto.response;

import implement.lld.model.expense.ExpenseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponseDto {
    private UUID id;
    private UUID payerId;
    private String description;
    private Double amount;
    private ExpenseType expenseType;
    private UUID groupId;
    private List<SplitResponseDto> splits;
}
