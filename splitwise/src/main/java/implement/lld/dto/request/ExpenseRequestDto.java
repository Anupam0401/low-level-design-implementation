package implement.lld.dto.request;

import implement.lld.model.expense.ExpenseType;
import implement.lld.model.split.SplitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class ExpenseRequestDto {
    @NotNull(message = "Payer ID is required")
    private UUID payerId;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;
    
    @NotNull(message = "Split type is required")
    private SplitType splitType;
    
    @NotNull(message = "Expense type is required")
    private ExpenseType expenseType;
    
    private UUID groupId;
    
    @NotNull(message = "Split details are required")
    private List<SplitRequestDto> splits;
}
