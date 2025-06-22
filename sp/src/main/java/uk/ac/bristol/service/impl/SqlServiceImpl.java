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
//
//    @Autowired
//    private UserMapper userMapper;
//    @Autowired
//    private WarningMapper warningMapper;
//    @Autowired
//    private AssetHolderMapper assetHolderMapper;
//    @Autowired
//    private AssetMapper assetMapper;
//
//    @Override
//    public List<UserAsAssetHolder> selectUserByAssetHolderId(Long id) {
//        return userMapper.selectUserByAssetHolderId(id);
//    }
//
//    @Override
//    public List<Warning> selectAllWarnings() {
//        return warningMapper.selectAllWarnings();
//    }
//
//    @Override
//    public List<UserAsAssetHolder> selectAllAssetHolders() {
//        return assetHolderMapper.selectAllAssetHolders();
//    }
//
//    @Override
//    public List<AssetHolder> selectAssetHolderByID(Long id) {
//        return assetHolderMapper.selectAssetHolderByID(id);
//    }
//
//    @Override
//    public List<AssetHolder> selectByAssetHolder(AssetHolder assetHolder) {
//        return assetHolderMapper.selectByAssetHolder(assetHolder);
//    }
//
//    @Override
//    public long insertAssetHolder(AssetHolder assetHolder) {
//        return assetHolderMapper.insertAssetHolder(assetHolder);
//    }
//
//    @Override
//    public long updateAssetHolder(AssetHolder assetHolder) {
//        assetHolder.setLastModified(Instant.now());
//        return assetHolderMapper.updateAssetHolder(assetHolder);
//    }
//
//    @Override
//    public long deleteByAssetHolder(AssetHolder assetHolder) {
//        List<Long> ids = assetHolderMapper.selectByAssetHolder(assetHolder).stream().map(AssetHolder::getId).toList();
//        if (ids.isEmpty()) return 0;
//        return assetHolderMapper.deleteByAssetHolderIDs(ids);
//    }
//
//    @Override
//    public long deleteByAssetHolderIDs(long[] ids) {
//        return assetHolderMapper.deleteByAssetHolderIDs(ids);
//    }
//
//    @Override
//    public long deleteByAssetHolderIDs(List<Long> ids) {
//        return assetHolderMapper.deleteByAssetHolderIDs(ids);
//    }
//
//    @Override
//    public List<AssetWithWeatherWarnings> selectAllAssets() {
//        return assetMapper.selectAllAssets();
//    }
//
//    @Override
//    public List<AssetWithWeatherWarnings> selectAssetByID(Long id) {
//        return assetMapper.selectAssetByID(id);
//    }
//
//    @Override
//    public List<AssetWithWeatherWarnings> selectByAsset(Asset asset) {
//        return assetMapper.selectByAsset(asset);
//    }
//
//    @Override
//    public List<AssetWithWeatherWarnings> selectAllAssetsOfHolder(Long id) {
//        return assetMapper.selectAllAssetsOfHolder(id);
//    }
//
//    @Override
//    public long insertAsset(Asset asset) {
//        return assetMapper.insertAsset(asset);
//    }
//
//    @Override
//    public long updateAsset(Asset asset) {
//        List<AssetWithWeatherWarnings> old = this.selectAssetByID(asset.getId());
//        if (old.size() != 1) return 0;
//        List<AssetHolder> assetHolder = this.selectAssetHolderByID(old.get(0).getAsset().getAssetHolderId());
//        if (assetHolder.size() != 1) return 0;
//        Instant now = Instant.now();
//        assetHolder.get(0).setLastModified(now);
//        asset.setLastModified(now);
//        return assetMapper.updateAsset(asset);
//    }
//
//    @Override
//    public long deleteByAsset(Asset asset) {
//        List<Long> ids = assetMapper.selectByAsset(asset).stream().map(a -> a.getAsset().getId()).toList();
//        if (ids.isEmpty()) return 0;
//        return assetMapper.deleteByAssetIDs(ids);
//    }
//
//    @Override
//    public long deleteByAssetIDs(long[] ids) {
//        return assetMapper.deleteByAssetIDs(ids);
//    }
//
//    @Override
//    public long deleteByAssetIDs(List<Long> ids) {
//        return assetMapper.deleteByAssetIDs(ids);
//    }
//
//    @Override
//    public long insertUser(UserAsAssetHolder uh) {
//        assetHolderMapper.insertAssetHolder(uh.getAssetHolder());
//        uh.getUser().setAssetHolderId(uh.getAssetHolder().getId());
//        return userMapper.insertUser(uh.getUser());
//    }
//
//    @Override
//    public long updateUser(UserAsAssetHolder uh) {
//        return 0;
//    }
}
