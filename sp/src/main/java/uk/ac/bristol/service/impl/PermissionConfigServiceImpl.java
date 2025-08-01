package uk.ac.bristol.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.PermissionConfigMapper;
import uk.ac.bristol.exception.SpExceptions;
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
    public List<PermissionConfig> getPermissionConfigs(Map<String, Object> filters,
                                                       List<Map<String, String>> orderList,
                                                       Integer limit,
                                                       Integer offset) {
        return permissionConfigMapper.selectAllPermissionConfigs(
                QueryTool.formatFilters(filters),
                QueryTool.filterOrderList("permission_config_row_id", orderList, "permission_configs"),
                limit, offset);
    }

    @Override
    public List<PermissionConfig> getCursoredPermissionConfigs(Long lastConfigRowId, Map<String, Object> filters, List<Map<String, String>> orderList, Integer limit, Integer offset) {
        return permissionConfigMapper.selectAllPermissionConfigs(
                QueryTool.formatCursoredDeepPageFilters("permission_config_row_id", lastConfigRowId, filters),
                QueryTool.filterOrderList("permission_config_row_id", orderList, "permission_configs"),
                limit, offset);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public PermissionConfig getPermissionConfigByUserId(String userId) {
        List<PermissionConfig> configs = permissionConfigMapper.selectPermissionConfigByUserId(userId);
        if (configs == null || configs.isEmpty()) {
            permissionConfigMapper.insertPermissionConfig(new PermissionConfig(userId));
        }
        List<PermissionConfig> list = permissionConfigMapper.selectPermissionConfigByUserId(userId);
        if (list.size() != 1) {
            throw new SpExceptions.SystemException("Found " + list.size() + " permission configs for user " + userId);
        }
        return list.get(0);
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
