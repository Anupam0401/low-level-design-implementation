package implement.lld.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class GroupRequestDto {
    @NotBlank(message = "Group name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Owner ID is required")
    private UUID ownerId;
    
    private List<UUID> memberIds;
}
