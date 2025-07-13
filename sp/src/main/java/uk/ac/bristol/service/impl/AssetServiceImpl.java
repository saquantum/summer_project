package uk.ac.bristol.service.impl;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.AssetHolderMapper;
import uk.ac.bristol.dao.AssetMapper;
import uk.ac.bristol.dao.MetaDataMapper;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.Asset;
import uk.ac.bristol.pojo.AssetHolder;
import uk.ac.bristol.pojo.AssetType;
import uk.ac.bristol.pojo.AssetWithWeatherWarnings;
import uk.ac.bristol.service.AssetService;
import uk.ac.bristol.service.ContactService;
import uk.ac.bristol.service.WarningService;
import uk.ac.bristol.util.QueryTool;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
@Service
public class AssetServiceImpl implements AssetService {

    private final MetaDataMapper metaDataMapper;
    private final AssetMapper assetMapper;
    private final AssetHolderMapper assetHolderMapper;
    private final WarningService warningService;
    private final ContactService contactService;

    public AssetServiceImpl(MetaDataMapper metaDataMapper, AssetMapper assetMapper, AssetHolderMapper assetHolderMapper, WarningService warningService, ContactService contactService) {
        this.metaDataMapper = metaDataMapper;
        this.assetMapper = assetMapper;
        this.assetHolderMapper = assetHolderMapper;
        this.warningService = warningService;
        this.contactService = contactService;
    }

    private Map<String, AssetType> getTypeMap() {
        return assetMapper.selectAllAssetTypes(null,null, null, null).stream()
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
    public List<Asset> getAllAssets(Map<String, Object> filters,
                                    List<Map<String, String>> orderList,
                                    Integer limit,
                                    Integer offset) {
        return this.prepareAssetList(assetMapper.selectAllAssets(
                QueryTool.formatFilters(filters),
                QueryTool.filterOrderList(orderList, "assets"),
                limit, offset), this.getTypeMap());
    }

    @Override
    public List<AssetWithWeatherWarnings> getAllAssetsWithWarnings(Map<String, Object> filters,
                                                                   List<Map<String, String>> orderList,
                                                                   Integer limit,
                                                                   Integer offset) {
        return this.prepareAWList(assetMapper.selectAllAssetsWithWarnings(
                QueryTool.formatFilters(filters),
                QueryTool.filterOrderList(orderList, "assets", "weather_warnings"),
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
        return this.prepareAssetList(assetMapper.selectByAsset(asset, QueryTool.filterOrderList(orderList, "assets"), limit, offset), this.getTypeMap());
    }

    @Override
    public List<AssetWithWeatherWarnings> getAssetWithWarningsByAsset(Asset asset,
                                                                      List<Map<String, String>> orderList,
                                                                      Integer limit,
                                                                      Integer offset) {

        return this.prepareAWList(assetMapper.selectByAssetWithWarnings(asset,
                QueryTool.filterOrderList(orderList, "assets", "weather_warnings"),
                limit, offset), this.getTypeMap());
    }

    @Override
    public List<Asset> getAllAssetsByAssetHolderId(String ownerId,
                                                   List<Map<String, String>> orderList,
                                                   Integer limit,
                                                   Integer offset) {
        return this.prepareAssetList(assetMapper.selectAllAssetsOfHolder(ownerId, QueryTool.filterOrderList(orderList, "assets"), limit, offset), this.getTypeMap());
    }

    @Override
    public List<AssetWithWeatherWarnings> getAllAssetsWithWarningsByAssetHolderId(String ownerId,
                                                                                  List<Map<String, String>> orderList,
                                                                                  Integer limit,
                                                                                  Integer offset) {
        return this.prepareAWList(assetMapper.selectAllAssetsWithWarningsOfHolder(ownerId,
                QueryTool.filterOrderList(orderList, "assets", "asset_types", "weather_warnings"),
                limit, offset), this.getTypeMap());
    }

    @Override
    public List<AssetType> getAllAssetTypes(Map<String, Object> filters,
                                            List<Map<String, String>> orderList,
                                            Integer limit,
                                            Integer offset) {
        return assetMapper.selectAllAssetTypes(
                QueryTool.formatFilters(filters),
                QueryTool.filterOrderList(orderList, "asset_types"),
                limit, offset);
    }

    @Override
    public int insertAssetType(AssetType assetType) {
        int n;
        if (assetType.getId() == null || assetType.getId().isEmpty()) {
            n = assetMapper.insertAssetTypeAutoId(assetType);
        } else {
            n = assetMapper.insertAssetType(assetType);
        }
        metaDataMapper.increaseTotalCountByTableName("asset_types", n);
        return n;
    }

    @Override
    public int insertAsset(Asset asset) {
        int n;
        asset.setLastModified(Instant.now());
        List<String> assetIds;
        if (asset.getId() == null || asset.getId().isEmpty()) {
            assetIds = assetMapper.insertAssetReturningId(asset);
            n = assetIds.size();
        } else {
            n = assetMapper.insertAsset(asset);
            metaDataMapper.increaseTotalCountByTableName("assets", n);
            return n;
        }

        metaDataMapper.increaseTotalCountByTableName("assets", n);

        List<Long> warningIds = warningService.selectWarningIdsByAssetId(assetIds.get(0));
        for (Long warningId : warningIds) {
            Map<String, Object> notification = contactService.formatNotification(warningId, assetIds.get(0));
            contactService.sendEmail(notification);
        }
        return n;
    }

    @Override
    public int updateAssetType(AssetType assetType) {
        return assetMapper.updateAssetType(assetType);
    }

    @Override
    public int updateAsset(Asset asset) {
        String ownerId = asset.getOwnerId();
        if (asset.getOwnerId() == null || asset.getOwnerId().isBlank()) {
            List<Asset> tmp = assetMapper.selectAssetByID(asset.getId());
            if (tmp.size() != 1) {
                throw new SpExceptions.GetMethodException("Found " + tmp.size() + " assets for asset id " + asset.getId() + " when updating asset");
            }
            ownerId = tmp.get(0).getOwnerId();
        }
        List<AssetHolder> list = assetHolderMapper.selectAssetHolderByIDs(List.of(ownerId));
        if (list.size() != 1) {
            throw new SpExceptions.GetMethodException(list.size() + " asset holders found for asset id " + asset.getId() + " when updating asset");
        }
        Instant now = Instant.now();
        list.get(0).setLastModified(now);
        asset.setLastModified(now);

        int value = assetMapper.updateAsset(asset);
        if (asset.getLocationAsJson() == null || asset.getLocationAsJson().isBlank()) {
            return value;
        }
        List<Long> warningIds = warningService.selectWarningIdsByAssetId(asset.getId());
        if (warningIds.isEmpty()) {
            return value;
        }
        for (Long warningId : warningIds) {
            Map<String, Object> notification = contactService.formatNotification(warningId, asset.getId());
            contactService.sendEmail(notification);
        }
        return value;

    }

    @Override
    public int deleteAssetTypeByIDs(String[] ids) {
        int n = assetMapper.deleteAssetTypeByIDs(ids);
        metaDataMapper.increaseTotalCountByTableName("asset_types", -n);
        return n;
    }

    @Override
    public int deleteAssetTypeByIDs(List<String> ids) {
        int n = assetMapper.deleteAssetTypeByIDs(ids);
        metaDataMapper.increaseTotalCountByTableName("asset_types", -n);
        return n;
    }

    @Override
    public int deleteAssetByIDs(String[] ids) {
        int n = assetMapper.deleteAssetByIDs(ids);
        metaDataMapper.increaseTotalCountByTableName("assets", -n);
        return n;
    }

    @Override
    public int deleteAssetByIDs(List<String> ids) {
        int n = assetMapper.deleteAssetByIDs(ids);
        metaDataMapper.increaseTotalCountByTableName("assets", -n);
        return n;
    }

    @Override
    public List<String> selectAssetIdsByWarningId(@Param("id") Long id){
        return assetMapper.selectAssetIdsWithWarningId(id);
    }
}
