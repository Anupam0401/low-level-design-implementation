package implement.lld.repository.impl;

import implement.lld.model.Group;
import implement.lld.model.User;
import implement.lld.repository.interfaces.IGroupRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of the IGroupRepository interface
 */
@Repository
public class InMemoryGroupRepository implements IGroupRepository {
    private final ConcurrentHashMap<UUID, Group> groups = new ConcurrentHashMap<>();

    @Override
    public Group save(Group group, UUID groupId) {
        groups.put(groupId, group);
        return group;
    }

    @Override
    public Group findById(UUID groupId) {
        return groups.get(groupId);
    }

    @Override
    public List<Group> findByOwnerId(UUID ownerId) {
        return groups.values().stream()
                .filter(group -> group.getOwner().getId().equals(ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> findByMemberId(UUID memberId) {
        return groups.values().stream()
                .filter(group -> group.getMembers().stream()
                        .anyMatch(member -> member.getId().equals(memberId)))
                .collect(Collectors.toList());
    }

    @Override
    public boolean addMember(UUID groupId, User user) {
        Group group = groups.get(groupId);
        if (group != null) {
            group.addMember(user);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeMember(UUID groupId, UUID userId) {
        Group group = groups.get(groupId);
        if (group != null) {
            List<User> updatedMembers = group.getMembers().stream()
                    .filter(member -> !member.getId().equals(userId))
                    .collect(Collectors.toList());
            
            // If owner is being removed, don't allow it
            if (group.getOwner().getId().equals(userId)) {
                return false;
            }
            
            // Replace members list
            group.getMembers().clear();
            updatedMembers.forEach(group::addMember);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteById(UUID groupId) {
        return groups.remove(groupId) != null;
    }
}
