package uk.ac.bristol.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.*;
import uk.ac.bristol.pojo.*;
import uk.ac.bristol.service.SqlService;
import uk.ac.bristol.util.JwtUtil;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
@Service
public class SqlServiceImpl implements SqlService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WarningMapper warningMapper;
    @Autowired
    private AssetHolderMapper assetHolderMapper;
    @Autowired
    private AssetMapper assetMapper;

    @Override
    public List<UserAsAssetHolder> selectUserByAssetHolderId(Integer id) {
        return userMapper.selectUserByAssetHolderId(id);
    }

    @Override
    public List<Warning> selectAllWarnings() {
        return warningMapper.selectAllWarnings();
    }

    @Override
    public List<UserAsAssetHolder> selectAllAssetHolders() {
        return assetHolderMapper.selectAllAssetHolders();
    }

    @Override
    public List<AssetHolder> selectAssetHolderByID(Integer id) {
        return assetHolderMapper.selectAssetHolderByID(id);
    }

    @Override
    public List<AssetHolder> selectByAssetHolder(AssetHolder assetHolder) {
        return assetHolderMapper.selectByAssetHolder(assetHolder);
    }

    @Override
    public int insertAssetHolder(AssetHolder assetHolder) {
        return assetHolderMapper.insertAssetHolder(assetHolder);
    }

    @Override
    public int updateAssetHolder(AssetHolder assetHolder) {
        assetHolder.setLastModified(Instant.now());
        return assetHolderMapper.updateAssetHolder(assetHolder);
    }

    @Override
    public int deleteByAssetHolder(AssetHolder assetHolder) {
        List<Integer> ids = assetHolderMapper.selectByAssetHolder(assetHolder).stream().map(AssetHolder::getId).toList();
        if (ids.isEmpty()) return 0;
        return assetHolderMapper.deleteByAssetHolderIDs(ids);
    }

    @Override
    public int deleteByAssetHolderIDs(int[] ids) {
        return assetHolderMapper.deleteByAssetHolderIDs(ids);
    }

    @Override
    public int deleteByAssetHolderIDs(List<Integer> ids) {
        return assetHolderMapper.deleteByAssetHolderIDs(ids);
    }

    @Override
    public List<AssetWithWeatherWarnings> selectAllAssets() {
        return assetMapper.selectAllAssets();
    }

    @Override
    public List<AssetWithWeatherWarnings> selectAssetByID(Integer id) {
        return assetMapper.selectAssetByID(id);
    }

    @Override
    public List<AssetWithWeatherWarnings> selectByAsset(Asset asset) {
        return assetMapper.selectByAsset(asset);
    }

    @Override
    public List<AssetWithWeatherWarnings> selectAllAssetsOfHolder(Integer id) {
        return assetMapper.selectAllAssetsOfHolder(id);
    }

    @Override
    public int insertAsset(Asset asset) {
        return assetMapper.insertAsset(asset);
    }

    @Override
    public int updateAsset(Asset asset) {
        List<AssetWithWeatherWarnings> old = this.selectAssetByID(asset.getId());
        if (old.size() != 1) return 0;
        List<AssetHolder> assetHolder = this.selectAssetHolderByID(old.get(0).getAsset().getAssetHolderId());
        if (assetHolder.size() != 1) return 0;
        Instant now = Instant.now();
        assetHolder.get(0).setLastModified(now);
        asset.setLastModified(now);
        return assetMapper.updateAsset(asset);
    }

    @Override
    public int deleteByAsset(Asset asset) {
        List<Integer> ids = assetMapper.selectByAsset(asset).stream().map(a -> a.getAsset().getId()).toList();
        if (ids.isEmpty()) return 0;
        return assetMapper.deleteByAssetIDs(ids);
    }

    @Override
    public int deleteByAssetIDs(int[] ids) {
        return assetMapper.deleteByAssetIDs(ids);
    }

    @Override
    public int deleteByAssetIDs(List<Integer> ids) {
        return assetMapper.deleteByAssetIDs(ids);
    }

    @Override
    public User login(User user) {
        List<User> list = userMapper.loginQuery(user);
        if (list.size() != 1) return null;
        User u = list.get(0);
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", u.getId());
        claims.put("username", u.getUsername());
        claims.put("assetHolderId", u.getAssetHolderId());
        claims.put("isAdmin", u.isAdmin());
        u.setToken(JwtUtil.generateJWT(claims));
        return u;
    }

    @Override
    public int insertUser(UserAsAssetHolder uh) {
        assetHolderMapper.insertAssetHolder(uh.getAssetHolder());
        uh.getUser().setAssetHolderId(uh.getAssetHolder().getId());
        return userMapper.insertUser(uh.getUser());
    }

    @Override
    public int updateUser(UserAsAssetHolder uh) {
        return 0;
    }
}
