package uk.ac.bristol.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.AssetHolderMapper;
import uk.ac.bristol.dao.AssetMapper;
import uk.ac.bristol.pojo.Asset;
import uk.ac.bristol.pojo.AssetHolder;
import uk.ac.bristol.pojo.AssetType;
import uk.ac.bristol.pojo.AssetWithWeatherWarnings;
import uk.ac.bristol.service.AssetService;
import uk.ac.bristol.util.QueryTool;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
@Service
public class AssetServiceImpl implements AssetService {

    private final AssetMapper assetMapper;
    private final AssetHolderMapper assetHolderMapper;

    public AssetServiceImpl(AssetMapper assetMapper, AssetHolderMapper assetHolderMapper) {
        this.assetMapper = assetMapper;
        this.assetHolderMapper = assetHolderMapper;
    }

    private Map<String, AssetType> getTypeMap() {
        return assetMapper.selectAllAssetTypes(null, null, null).stream()
                .collect(Collectors.toMap(AssetType::getId, type -> type));
    }

    private Asset prepareAsset(Asset asset, Map<String, AssetType> typeMap) {
        asset.setType(typeMap.get(asset.getTypeId()));
        return asset;
    }

    private List<Asset> prepareAssetList(List<Asset> list, Map<String, AssetType> typeMap) {
        list.forEach(asset -> this.prepareAsset(asset, typeMap));
        return list;
    }

    private List<AssetWithWeatherWarnings> prepareAWList(List<AssetWithWeatherWarnings> list, Map<String, AssetType> typeMap) {
        list.forEach(aw -> aw.setAsset(this.prepareAsset(aw.getAsset(), typeMap)));
        return list;
    }

    @Override
    public List<Asset> getAllAssets(List<Map<String, String>> orderList,
                                    Integer limit,
                                    Integer offset) {
        return this.prepareAssetList(assetMapper.selectAllAssets(QueryTool.filterOrderList(orderList, "asset"), limit, offset), this.getTypeMap());
    }

    @Override
    public List<AssetWithWeatherWarnings> getAllAssetsWithWarnings(List<Map<String, String>> orderList,
                                                                   Integer limit,
                                                                   Integer offset) {
        return this.prepareAWList(assetMapper.selectAllAssetsWithWarnings(
                QueryTool.filterOrderList(orderList, "asset", "warning"),
                limit, offset), this.getTypeMap());
    }

    @Override
    public List<Asset> getAssetById(String id) {
        return this.prepareAssetList(assetMapper.selectAssetByID(id), this.getTypeMap());
    }

    @Override
    public List<AssetWithWeatherWarnings> getAssetWithWarningsById(String id) {
        return this.prepareAWList(assetMapper.selectAssetWithWarningsByID(id), this.getTypeMap());
    }

    @Override
    public List<Asset> getAssetByAsset(Asset asset,
                                       List<Map<String, String>> orderList,
                                       Integer limit,
                                       Integer offset) {
        return this.prepareAssetList(assetMapper.selectByAsset(asset, QueryTool.filterOrderList(orderList, "asset"), limit, offset), this.getTypeMap());
    }

    @Override
    public List<AssetWithWeatherWarnings> getAssetWithWarningsByAsset(Asset asset,
                                                                      List<Map<String, String>> orderList,
                                                                      Integer limit,
                                                                      Integer offset) {

        return this.prepareAWList(assetMapper.selectByAssetWithWarnings(asset,
                QueryTool.filterOrderList(orderList, "asset", "warning"),
                limit, offset), this.getTypeMap());
    }

    @Override
    public List<Asset> getAllAssetsByAssetHolderId(String ownerId,
                                                   List<Map<String, String>> orderList,
                                                   Integer limit,
                                                   Integer offset) {
        return this.prepareAssetList(assetMapper.selectAllAssetsOfHolder(ownerId, QueryTool.filterOrderList(orderList, "asset"), limit, offset), this.getTypeMap());
    }

    @Override
    public List<AssetWithWeatherWarnings> getAllAssetsWithWarningsByAssetHolderId(String ownerId,
                                                                                  List<Map<String, String>> orderList,
                                                                                  Integer limit,
                                                                                  Integer offset) {
        return this.prepareAWList(assetMapper.selectAllAssetsWithWarningsOfHolder(ownerId,
                QueryTool.filterOrderList(orderList, "asset", "asset_type", "warning"),
                limit, offset), this.getTypeMap());
    }

    @Override
    public List<AssetType> getAllAssetTypes(List<Map<String, String>> orderList,
                                            Integer limit,
                                            Integer offset) {
        return assetMapper.selectAllAssetTypes(QueryTool.filterOrderList(orderList, "asset_type"), limit, offset);
    }

    @Override
    public int insertAssetType(AssetType assetType) {
        return assetMapper.insertAssetType(assetType);
    }

    @Override
    public int insertAsset(Asset asset) {
        asset.setLastModified(Instant.now());
        return assetMapper.insertAsset(asset);
    }

    @Override
    public int updateAssetType(AssetType assetType) {
        return assetMapper.updateAssetType(assetType);
    }

    @Override
    public int updateAsset(Asset asset) {
        List<AssetHolder> list = assetHolderMapper.selectAssetHolderByIDs(List.of(asset.getOwnerId()), null, null, null);
        if (list.size() != 1)
            throw new RuntimeException(list.size() + " asset holders found for asset id " + asset.getId() + " when updating asset");
        Instant now = Instant.now();
        list.get(0).setLastModified(now);
        asset.setLastModified(now);
        return assetMapper.updateAsset(asset);
    }

    @Override
    public int deleteAssetTypeByIDs(String[] ids) {
        return assetMapper.deleteAssetTypeByIDs(ids);
    }

    @Override
    public int deleteAssetTypeByIDs(List<String> ids) {
        return assetMapper.deleteAssetTypeByIDs(ids);
    }

    @Override
    public int deleteAssetByIDs(String[] ids) {
        return assetMapper.deleteAssetByIDs(ids);
    }

    @Override
    public int deleteAssetByIDs(List<String> ids) {
        return assetMapper.deleteAssetByIDs(ids);
    }
}
