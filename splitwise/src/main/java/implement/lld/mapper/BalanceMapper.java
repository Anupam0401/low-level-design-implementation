package implement.lld.mapper;

import implement.lld.dto.response.BalanceResponseDto;
import implement.lld.model.Balance;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mapper class to convert between Balance entity and Balance DTOs
 */
@Component
public class BalanceMapper {

    /**
     * Converts a Balance entity to a BalanceResponseDto
     * @param userId the user ID
     * @param balances the map of balances
     * @return the balance response DTO
     */
    public BalanceResponseDto toDto(UUID userId, ConcurrentHashMap<UUID, Double> balances) {
        return BalanceResponseDto.builder()
                .userId(userId)
                .balances(balances)
                .build();
    }
    
    /**
     * Converts a Balance entity to a BalanceResponseDto
     * @param userId the user ID
     * @param balances the map of balances
     * @return the balance response DTO
     */
    public BalanceResponseDto toDto(UUID userId, Map<UUID, Double> balances) {
        ConcurrentHashMap<UUID, Double> concurrentBalances = new ConcurrentHashMap<>();
        if (balances != null) {
            concurrentBalances.putAll(balances);
        }
        return BalanceResponseDto.builder()
                .userId(userId)
                .balances(concurrentBalances)
                .build();
    }
}
