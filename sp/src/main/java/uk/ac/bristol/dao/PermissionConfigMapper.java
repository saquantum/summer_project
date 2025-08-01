package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import uk.ac.bristol.pojo.PermissionConfig;

import java.util.List;
import java.util.Map;

@Mapper
public interface PermissionConfigMapper {

    List<PermissionConfig> selectAllPermissionConfigs(@Param("filterString") String filterString,
                                                      @Param("orderList") List<Map<String, String>> orderList,
                                                      @Param("limit") Integer limit,
                                                      @Param("offset") Integer offset);

    List<PermissionConfig> selectPermissionConfigByUserId(@Param("userId") String userId);

    List<PermissionConfig> selectPermissionConfigByAssetHolderId(@Param("assetHolderId") String assetHolderId);

    int insertPermissionConfig(PermissionConfig permissionConfig);

    int updatePermissionConfigByUserId(PermissionConfig permissionConfig);
}
