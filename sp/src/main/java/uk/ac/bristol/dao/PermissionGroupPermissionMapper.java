package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import uk.ac.bristol.pojo.PermissionGroupPermission;

@Mapper
public interface PermissionGroupPermissionMapper {
    PermissionGroupPermission selectPermissionsByGroupId(Long groupId);
    void insertPermissions(PermissionGroupPermission permissions);
    void updatePermissions(PermissionGroupPermission permissions);
    void deletePermissions(Long groupId);
}