package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import uk.ac.bristol.pojo.FilterItemDTO;
import uk.ac.bristol.pojo.PermissionConfig;

import java.util.List;
import java.util.Map;

@Mapper
public interface PermissionConfigMapper {

    List<PermissionConfig> selectAllPermissionConfigs(@Param("filterList") List<FilterItemDTO> filterList,
                                                      @Param("orderList") List<Map<String, String>> orderList,
                                                      @Param("limit") Integer limit,
                                                      @Param("offset") Integer offset);

    List<PermissionConfig> selectPermissionConfigByUserId(@Param("userId") String userId);

    int insertPermissionConfig(PermissionConfig permissionConfig);

    int updatePermissionConfigByUserId(PermissionConfig permissionConfig);
}
