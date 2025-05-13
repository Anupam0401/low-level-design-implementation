package implement.lld.repository.interfaces;

import implement.lld.model.Group;
import implement.lld.model.User;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Group entity operations
 */
public interface IGroupRepository {
    /**
     * Save a group to the repository
     * @param group the group to save
     * @param groupId the group ID
     * @return the saved group
     */
    Group save(Group group, UUID groupId);
    
    /**
     * Find a group by ID
     * @param groupId the group ID
     * @return the group if found, null otherwise
     */
    Group findById(UUID groupId);
    
    /**
     * Find groups by owner
     * @param ownerId the owner user ID
     * @return list of groups owned by the user
     */
    List<Group> findByOwnerId(UUID ownerId);
    
    /**
     * Find groups by member
     * @param memberId the member user ID
     * @return list of groups that the user is a member of
     */
    List<Group> findByMemberId(UUID memberId);
    
    /**
     * Add a member to a group
     * @param groupId the group ID
     * @param user the user to add
     * @return true if added, false otherwise
     */
    boolean addMember(UUID groupId, User user);
    
    /**
     * Remove a member from a group
     * @param groupId the group ID
     * @param userId the user ID to remove
     * @return true if removed, false otherwise
     */
    boolean removeMember(UUID groupId, UUID userId);
    
    /**
     * Delete a group by ID
     * @param groupId the group ID
     * @return true if deleted, false otherwise
     */
    boolean deleteById(UUID groupId);
}
