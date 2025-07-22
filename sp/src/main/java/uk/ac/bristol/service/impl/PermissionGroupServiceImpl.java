package uk.ac.bristol.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.PermissionGroupMapper;
import uk.ac.bristol.pojo.PermissionGroup;
import uk.ac.bristol.service.PermissionGroupService;

import java.util.List;

@Service
public class PermissionGroupServiceImpl implements PermissionGroupService {

    private final PermissionGroupMapper permissionGroupMapper;

    public PermissionGroupServiceImpl(PermissionGroupMapper permissionGroupMapper) {
        this.permissionGroupMapper = permissionGroupMapper;
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
        permissionGroupMapper.deleteGroup(groupId);
    }
}