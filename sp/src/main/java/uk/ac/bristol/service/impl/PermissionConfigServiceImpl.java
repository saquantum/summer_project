package uk.ac.bristol.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.bristol.dao.PermissionConfigMapper;
import uk.ac.bristol.pojo.PermissionConfig;

@Service
public class PermissionConfigServiceImpl implements PermissionConfigService {

    @Autowired
    private PermissionConfigMapper permissionConfigMapper;

    @Override
    public PermissionConfig getPermissionByUserId(Long userId) {
        return permissionConfigMapper.findByUserId(userId);
    }

    @Override
    public void updatePermission(PermissionConfig permissionConfig) {
        permissionConfigMapper.update(permissionConfig);
    }

    @Override
    public void insertPermission(PermissionConfig permissionConfig) {
        permissionConfigMapper.insert(permissionConfig);
    }
}
