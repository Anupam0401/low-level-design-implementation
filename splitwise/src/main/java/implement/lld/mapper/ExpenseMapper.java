package implement.lld.mapper;

import implement.lld.dto.request.ExpenseRequestDto;
import implement.lld.dto.request.SplitRequestDto;
import implement.lld.dto.response.ExpenseResponseDto;
import implement.lld.dto.response.SplitResponseDto;
import implement.lld.model.expense.Expense;
import implement.lld.model.split.EqualSplit;
import implement.lld.model.split.ExactSplit;
import implement.lld.model.split.PercentSplit;
import implement.lld.model.split.Split;
import implement.lld.model.split.SplitType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper class to convert between Expense entity and Expense DTOs
 */
@Component
public class ExpenseMapper {

    /**
     * Converts a list of SplitRequestDto to a list of Split entities
     * @param splitRequestDtos the list of split request DTOs
     * @param splitType the type of split
     * @return the list of Split entities
     */
    public List<Split> toSplitEntities(List<SplitRequestDto> splitRequestDtos, SplitType splitType) {
        List<Split> splits = new ArrayList<>();
        
        for (SplitRequestDto splitRequestDto : splitRequestDtos) {
            Split split = switch (splitType) {
                case EQUAL -> new EqualSplit(splitRequestDto.getUserId());
                case EXACT -> new ExactSplit(splitRequestDto.getUserId(), splitRequestDto.getAmount());
                case PERCENT -> new PercentSplit(splitRequestDto.getUserId(), splitRequestDto.getPercent());
            };
            splits.add(split);
        }
        
        return splits;
    }

    /**
     * Converts a list of Split entities to a list of SplitResponseDto
     * @param splits the list of Split entities
     * @return the list of split response DTOs
     */
    public List<SplitResponseDto> toSplitDtos(List<Split> splits) {
        return splits.stream()
                .map(split -> SplitResponseDto.builder()
                        .userId(split.getUserId())
                        .amount(split.getAmount())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Converts an Expense entity to an ExpenseResponseDto
     * @param expense the Expense entity
     * @return the expense response DTO
     */
    public ExpenseResponseDto toDto(Expense expense) {
        return ExpenseResponseDto.builder()
                .id(expense.getId())
                .payerId(expense.getPayerId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .expenseType(expense.getExpenseType())
                .groupId(expense.getGroupId())
                .splits(toSplitDtos(expense.getSplits()))
                .build();
    }

    /**
     * Converts a list of Expense entities to a list of ExpenseResponseDtos
     * @param expenses the list of Expense entities
     * @return the list of expense response DTOs
     */
    public List<ExpenseResponseDto> toDtoList(List<Expense> expenses) {
        return expenses.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
