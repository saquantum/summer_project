package uk.ac.bristol.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.PermissionConfigMapper;
import uk.ac.bristol.pojo.PermissionConfig;
import uk.ac.bristol.service.PermissionConfigService;
import uk.ac.bristol.util.QueryTool;

import java.util.List;
import java.util.Map;

@Service
public class PermissionConfigServiceImpl implements PermissionConfigService {

    private final PermissionConfigMapper permissionConfigMapper;

    public PermissionConfigServiceImpl(PermissionConfigMapper permissionConfigMapper) {
        this.permissionConfigMapper = permissionConfigMapper;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<PermissionConfig> getAllPermissionConfigs(Map<String, Object> filters,
                                                          List<Map<String, String>> orderList,
                                                          Integer limit,
                                                          Integer offset) {
        return permissionConfigMapper.selectAllPermissionConfigs(
                QueryTool.formatFilters(filters),
                QueryTool.filterOrderList(orderList, "permission_configs"),
                limit, offset);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<PermissionConfig> getPermissionConfigByUserId(String userId) {
        List<PermissionConfig> configs = permissionConfigMapper.selectPermissionConfigByUserId(userId);
        if (configs == null || configs.isEmpty()) {
            permissionConfigMapper.insertPermissionConfig(new PermissionConfig(userId));
        }
        return permissionConfigMapper.selectPermissionConfigByUserId(userId);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<PermissionConfig> getPermissionConfigByAssetHolderId(String assetHolderId) {
        return permissionConfigMapper.selectPermissionConfigByAssetHolderId(assetHolderId);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int insertPermissionConfig(PermissionConfig permissionConfig) {
        return permissionConfigMapper.insertPermissionConfig(permissionConfig);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int updatePermissionConfigByUserId(PermissionConfig permissionConfig) {
        return permissionConfigMapper.updatePermissionConfigByUserId(permissionConfig);
    }
}
