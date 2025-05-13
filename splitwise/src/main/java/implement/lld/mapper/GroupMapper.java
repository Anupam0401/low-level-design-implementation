package implement.lld.mapper;

import implement.lld.dto.response.GroupResponseDto;
import implement.lld.model.Group;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Mapper class to convert between Group entity and Group DTOs
 */
@Component
public class GroupMapper {
    private final UserMapper userMapper;

    public GroupMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * Converts a Group entity to a GroupResponseDto
     * @param group the Group entity
     * @return the group response DTO
     */
    public GroupResponseDto toDto(Group group) {
        return GroupResponseDto.builder()
                .id(UUID.randomUUID()) // In a real implementation, this would be the group's ID from the database
                .name(group.getName())
                .description(group.getDescription())
                .owner(userMapper.toDto(group.getOwner()))
                .members(userMapper.toDtoList(group.getMembers()))
                .build();
    }

    /**
     * Converts a list of Group entities to a list of GroupResponseDtos
     * @param groups the list of Group entities
     * @return the list of group response DTOs
     */
    public List<GroupResponseDto> toDtoList(List<Group> groups) {
        return groups.stream()
                .map(this::toDto)
                .toList();
    }
}
