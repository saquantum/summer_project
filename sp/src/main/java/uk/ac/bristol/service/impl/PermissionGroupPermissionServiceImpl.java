package uk.ac.bristol.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.PermissionGroupPermissionMapper;
import uk.ac.bristol.pojo.PermissionGroupPermission;
import uk.ac.bristol.service.PermissionGroupPermissionService;

@Service
public class PermissionGroupPermissionServiceImpl implements PermissionGroupPermissionService {

    private final PermissionGroupPermissionMapper permissionGroupPermissionMapper;

    public PermissionGroupPermissionServiceImpl(PermissionGroupPermissionMapper permissionGroupPermissionMapper) {
        this.permissionGroupPermissionMapper = permissionGroupPermissionMapper;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public PermissionGroupPermission getPermissionsByGroupId(Long groupId) {
        return permissionGroupPermissionMapper.selectPermissionsByGroupId(groupId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void addPermissions(PermissionGroupPermission permissions) {
        permissionGroupPermissionMapper.insertPermissions(permissions);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updatePermissions(PermissionGroupPermission permissions) {
        permissionGroupPermissionMapper.updatePermissions(permissions);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void deletePermissions(Long groupId) {
        permissionGroupPermissionMapper.deletePermissions(groupId);
    }
}