package implement.lld.mapper;

import implement.lld.dto.response.TransactionResponseDto;
import implement.lld.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper class to convert between Transaction entity and Transaction DTOs
 */
@Component
public class TransactionMapper {

    /**
     * Converts a Transaction entity to a TransactionResponseDto
     * @param transaction the Transaction entity
     * @return the transaction response DTO
     */
    public TransactionResponseDto toDto(Transaction transaction) {
        return TransactionResponseDto.builder()
                .transactionId(transaction.getTransactionId())
                .senderId(transaction.getSenderId())
                .receiverId(transaction.getReceiverId())
                .amount(transaction.getAmount())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    /**
     * Converts a list of Transaction entities to a list of TransactionResponseDtos
     * @param transactions the list of Transaction entities
     * @return the list of transaction response DTOs
     */
    public List<TransactionResponseDto> toDtoList(List<Transaction> transactions) {
        return transactions.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
