package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import uk.ac.bristol.pojo.PermissionGroupPermission;

import java.util.List;

@Mapper
public interface PermissionGroupPermissionMapper {
    List<PermissionGroupPermission> selectPermissionsByGroupId(Long groupId);
    void insertPermissions(PermissionGroupPermission permissions);
    void updatePermissions(PermissionGroupPermission permissions);
    void deletePermissions(Long groupId);
}