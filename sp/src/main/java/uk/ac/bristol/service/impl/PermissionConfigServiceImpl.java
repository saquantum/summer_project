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
        return permissionConfigMapper.selectPermissionConfigs(
                QueryTool.formatFilters(filters),
                QueryTool.formatOrderList("permission_config_row_id", orderList, "permission_configs"),
                limit, offset);
    }

    @Override
    public List<PermissionConfig> getCursoredPermissionConfigs(Long lastConfigRowId, Map<String, Object> filters, List<Map<String, String>> orderList, Integer limit, Integer offset) {
        Map<String, Object> anchor = null;
        if (lastConfigRowId != null) {
            List<Map<String, Object>> list = permissionConfigMapper.selectPermissionConfigAnchor(lastConfigRowId);
            if (list.size() != 1) {
                throw new SpExceptions.GetMethodException("Found " + list.size() + " anchors using permission config row id " + lastConfigRowId);
            }
            anchor = list.get(0);
        }
        List<Map<String, String>> formattedOrderList = QueryTool.formatOrderList("permission_config_row_id", orderList, "permission_configs");
        return permissionConfigMapper.selectPermissionConfigs(
                QueryTool.formatCursoredDeepPageFilters(filters, anchor, formattedOrderList),
                formattedOrderList,
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
