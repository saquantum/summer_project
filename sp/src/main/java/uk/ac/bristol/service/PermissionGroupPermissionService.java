package uk.ac.bristol.service;

import uk.ac.bristol.pojo.PermissionGroupPermission;

public interface PermissionGroupPermissionService {

    PermissionGroupPermission getPermissionsByGroupId(Long groupId);

    void addPermissions(PermissionGroupPermission permissions);

    void updatePermissions(PermissionGroupPermission permissions);

    void deletePermissions(Long groupId);
}