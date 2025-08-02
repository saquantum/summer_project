package uk.ac.bristol.service;

import uk.ac.bristol.pojo.PermissionGroupPermission;

import java.util.List;

public interface PermissionGroupPermissionService {

    List<PermissionGroupPermission> getPermissionsByGroupId(Long groupId);

    void addPermissions(PermissionGroupPermission permissions);

    void updatePermissions(PermissionGroupPermission permissions);

    void deletePermissions(Long groupId);
}