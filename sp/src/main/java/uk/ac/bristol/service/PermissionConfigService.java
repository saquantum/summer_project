package uk.ac.bristol.service;

import uk.ac.bristol.pojo.PermissionConfig;

import java.util.List;
import java.util.Map;

public interface PermissionConfigService {

    List<PermissionConfig> getAllPermissionConfigs(List<Map<String, String>> orderList,
                                                   Integer limit,
                                                   Integer offset);

    List<PermissionConfig> getPermissionConfigByUserId(String userId);

    int insertPermissionConfig(PermissionConfig permissionConfig);

    int updatePermissionConfigByUserId(PermissionConfig permissionConfig);
}
