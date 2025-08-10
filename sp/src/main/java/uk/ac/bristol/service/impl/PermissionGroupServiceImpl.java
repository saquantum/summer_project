package uk.ac.bristol.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.GroupMemberMapper;
import uk.ac.bristol.dao.PermissionGroupMapper;
import uk.ac.bristol.dao.PermissionGroupPermissionMapper;
import uk.ac.bristol.pojo.CreateGroupRequest;
import uk.ac.bristol.pojo.PermissionGroup;
import uk.ac.bristol.pojo.PermissionGroupPermission;
import uk.ac.bristol.service.PermissionGroupService;

import java.util.List;

@Service
public class PermissionGroupServiceImpl implements PermissionGroupService {

    private static final Long DEFAULT_GROUP_ID = 1L;

    private final PermissionGroupMapper permissionGroupMapper;
    private final PermissionGroupPermissionMapper permissionGroupPermissionMapper;
    private final GroupMemberMapper groupMemberMapper;

    public PermissionGroupServiceImpl(PermissionGroupMapper permissionGroupMapper,
                                      PermissionGroupPermissionMapper permissionGroupPermissionMapper,
                                      GroupMemberMapper groupMemberMapper) {
        this.permissionGroupMapper = permissionGroupMapper;
        this.permissionGroupPermissionMapper = permissionGroupPermissionMapper;
        this.groupMemberMapper = groupMemberMapper;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<PermissionGroup> getAllGroups() {
        return permissionGroupMapper.selectAllGroups();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public PermissionGroup getGroupById(Long groupId) {
        return permissionGroupMapper.selectGroupById(groupId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void addGroup(PermissionGroup group) {
        permissionGroupMapper.insertGroup(group);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateGroup(PermissionGroup group) {
        permissionGroupMapper.updateGroup(group);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void deleteGroup(Long groupId) {

        if (DEFAULT_GROUP_ID.equals(groupId)) {
            throw new IllegalArgumentException("cannot delete");
        }

        List<String> userIds = groupMemberMapper.findUserIdsByGroupId(groupId);
        if (userIds != null && !userIds.isEmpty()) {
            for (String userId : userIds) {

                groupMemberMapper.updateUserGroup(userId, DEFAULT_GROUP_ID);
            }
        }

        permissionGroupPermissionMapper.deletePermissions(groupId);

        permissionGroupMapper.deleteGroup(groupId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public PermissionGroup createGroupWithPermissions(CreateGroupRequest request) {
        PermissionGroup group = new PermissionGroup();
        group.setGroupName(request.getGroupName());
        group.setDescription(request.getDescription());

        permissionGroupMapper.insertGroup(group);

        PermissionGroupPermission permissions = new PermissionGroupPermission();
        permissions.setGroupId(group.getGroupId());
        permissions.setCanCreateAsset(request.getCanCreateAsset());
        permissions.setCanSetPolygonOnCreate(request.getCanSetPolygonOnCreate());
        permissions.setCanUpdateAssetFields(request.getCanUpdateAssetFields());
        permissions.setCanUpdateAssetPolygon(request.getCanUpdateAssetPolygon());
        permissions.setCanDeleteAsset(request.getCanDeleteAsset());
        permissions.setCanUpdateProfile(request.getCanUpdateProfile());

        permissionGroupPermissionMapper.insertPermissions(permissions);

        return group;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public PermissionGroup getGroupByName(String groupName) {
        return permissionGroupMapper.selectGroupByName(groupName);
    }
}
