package uk.ac.bristol.service;

import uk.ac.bristol.pojo.PermissionConfig;

import java.util.List;
import java.util.Map;

public interface PermissionConfigService {

    List<PermissionConfig> getPermissionConfigs(Map<String, Object> filters,
                                                List<Map<String, String>> orderList,
                                                Integer limit,
                                                Integer offset);

    List<PermissionConfig> getCursoredPermissionConfigs(Long lastConfigRowId,
                                                        Map<String, Object> filters,
                                                        List<Map<String, String>> orderList,
                                                        Integer limit,
                                                        Integer offset);

    PermissionConfig getPermissionConfigByUserId(String userId);

    int insertPermissionConfig(PermissionConfig permissionConfig);

    int updatePermissionConfigByUserId(PermissionConfig permissionConfig);
}
