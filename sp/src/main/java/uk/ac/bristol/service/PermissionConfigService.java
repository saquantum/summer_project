package uk.ac.bristol.service;

import uk.ac.bristol.pojo.PermissionConfig;

public interface PermissionConfigService {
    PermissionConfig getPermissionByUserId(Long userId);
    void updatePermission(PermissionConfig permissionConfig);
    void insertPermission(PermissionConfig permissionConfig);
}
