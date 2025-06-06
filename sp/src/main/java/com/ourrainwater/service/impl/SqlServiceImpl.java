package com.ourrainwater.service.impl;

import com.ourrainwater.dao.SqlMapper;
import com.ourrainwater.pojo.Asset;
import com.ourrainwater.pojo.AssetHolder;
import com.ourrainwater.service.SqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SqlServiceImpl implements SqlService {

    @Autowired
    private SqlMapper sqlMapper;

    @Override
    public List<AssetHolder> selectAllAssetHolders() { return sqlMapper.selectAllAssetHolders();}

    @Override
    public List<AssetHolder> selectAssetHolderByID(Integer id) {
        return sqlMapper.selectAssetHolderByID(id);
    }

    @Override
    public List<AssetHolder> selectByAssetHolder(AssetHolder assetHolder) {
        return sqlMapper.selectByAssetHolder(assetHolder);
    }

    @Override
    public int insertAssetHolder(AssetHolder assetHolder) {
        return sqlMapper.insertAssetHolder(assetHolder);
    }

    @Override
    public int updateAssetHolder(AssetHolder assetHolder) {
        return sqlMapper.updateAssetHolder(assetHolder);
    }

    @Override
    public int deleteByAssetHolder(AssetHolder assetHolder) {
        List<Integer> ids = sqlMapper.selectByAssetHolder(assetHolder).stream().map(AssetHolder::getId).toList();
        if (ids.isEmpty()) return 0;
        return sqlMapper.deleteByAssetHolderIDs(ids);
    }

    @Override
    public int deleteByAssetHolderIDs(int[] ids) {
        return sqlMapper.deleteByAssetHolderIDs(ids);
    }

    @Override
    public int deleteByAssetHolderIDs(List<Integer> ids) {
        return sqlMapper.deleteByAssetHolderIDs(ids);
    }

    @Override
    public List<Asset> selectAllAssets(){ return sqlMapper.selectAllAssets();}

    @Override
    public List<Asset> selectAssetByID(Integer id) {
        return sqlMapper.selectAssetByID(id);
    }

    @Override
    public List<Asset> selectByAsset(Asset asset) {
        return sqlMapper.selectByAsset(asset);
    }

    @Override
    public int insertAsset(Asset asset) {
        return sqlMapper.insertAsset(asset);
    }

    @Override
    public int updateAsset(Asset asset) {
        return sqlMapper.updateAsset(asset);
    }

    @Override
    public int deleteByAsset(Asset asset) {
        List<Integer> ids = sqlMapper.selectByAsset(asset).stream().map(Asset::getId).toList();
        if (ids.isEmpty()) return 0;
        return sqlMapper.deleteByAssetIDs(ids);
    }

    @Override
    public int deleteByAssetIDs(int[] ids) {
        return sqlMapper.deleteByAssetIDs(ids);
    }

    @Override
    public int deleteByAssetIDs(List<Integer> ids) {
        return sqlMapper.deleteByAssetIDs(ids);
    }

}
