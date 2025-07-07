package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import uk.ac.bristol.pojo.PermissionConfig;

@Mapper
public interface PermissionConfigMapper {

    PermissionConfig findByUserId(@Param("userId") Long userId);

    void insert(PermissionConfig permissionConfig);

    void update(PermissionConfig permissionConfig);
}
