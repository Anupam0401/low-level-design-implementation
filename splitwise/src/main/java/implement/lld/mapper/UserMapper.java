package implement.lld.mapper;

import implement.lld.dto.request.UserRequestDto;
import implement.lld.dto.response.UserResponseDto;
import implement.lld.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper class to convert between User entity and User DTOs
 */
@Component
public class UserMapper {

    /**
     * Converts a UserRequestDto to a User entity
     * @param userRequestDto the request DTO
     * @return the User entity
     */
    public User toEntity(UserRequestDto userRequestDto) {
        return new User(
                UUID.randomUUID(),
                userRequestDto.getName(),
                userRequestDto.getEmail()
        );
    }

    /**
     * Converts a User entity to a UserResponseDto
     * @param user the User entity
     * @return the response DTO
     */
    public UserResponseDto toDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    /**
     * Converts a list of User entities to a list of UserResponseDtos
     * @param users the list of User entities
     * @return the list of response DTOs
     */
    public List<UserResponseDto> toDtoList(List<User> users) {
        return users.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
