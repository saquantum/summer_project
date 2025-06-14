package uk.ac.bristol.service.impl;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.SqlMapper;
import uk.ac.bristol.pojo.Asset;
import uk.ac.bristol.pojo.AssetHolder;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.service.SqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.bristol.util.JwtUtil;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
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
        assetHolder.setLastModified(Instant.now());
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
    public List<Asset> selectAllAssetsOfHolder(Integer id) {
        return sqlMapper.selectAllAssetsOfHolder(id);
    }

    @Override
    public int insertAsset(Asset asset) {
        return sqlMapper.insertAsset(asset);
    }

    @Override
    public int updateAsset(Asset asset) {
        List<Asset> old = this.selectByAsset(asset);
        if(old.size() != 1) return 0;
        List<AssetHolder> assetHolder = this.selectAssetHolderByID(old.get(0).getAssetHolderId());
        if(assetHolder.size() != 1) return 0;
        Instant now = Instant.now();
        assetHolder.get(0).setLastModified(now);
        asset.setLastModified(now);
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

    @Override
    public User login(User user){
        List<User> list = sqlMapper.loginQuery(user);
        if(list.size() != 1) return null;
        User u = list.get(0);
        Map<String,Object> claims = new HashMap<>();
        claims.put("id", u.getId());
        claims.put("username", u.getUsername());
        claims.put("assetHolderId", u.getAssetHolderId());
        claims.put("isAdmin", u.isAdmin());
        u.setToken(JwtUtil.generateJWT(claims));
        return u;
    }
}
