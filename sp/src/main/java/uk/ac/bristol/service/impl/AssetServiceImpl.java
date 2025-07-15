package uk.ac.bristol.service.impl;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.AssetHolderMapper;
import uk.ac.bristol.dao.AssetMapper;
import uk.ac.bristol.dao.MetaDataMapper;
import uk.ac.bristol.dao.WarningMapper;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.Asset;
import uk.ac.bristol.pojo.AssetHolder;
import uk.ac.bristol.pojo.AssetType;
import uk.ac.bristol.pojo.AssetWithWeatherWarnings;
import uk.ac.bristol.service.AssetService;
import uk.ac.bristol.service.ContactService;
import uk.ac.bristol.util.QueryTool;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class AssetServiceImpl implements AssetService {

    private final MetaDataMapper metaDataMapper;
    private final AssetMapper assetMapper;
    private final AssetHolderMapper assetHolderMapper;
    private final WarningMapper warningMapper;
    private final ContactService contactService;

    public AssetServiceImpl(MetaDataMapper metaDataMapper, AssetMapper assetMapper, AssetHolderMapper assetHolderMapper, WarningMapper warningMapper, ContactService contactService) {
        this.metaDataMapper = metaDataMapper;
        this.assetMapper = assetMapper;
        this.assetHolderMapper = assetHolderMapper;
        this.warningMapper = warningMapper;
        this.contactService = contactService;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<Asset> getAllAssets(Map<String, Object> filters,
                                    List<Map<String, String>> orderList,
                                    Integer limit,
                                    Integer offset) {
        return assetMapper.selectAssets(
                QueryTool.formatFilters(filters),
                QueryTool.filterOrderList(orderList, "assets"),
                limit, offset);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<AssetWithWeatherWarnings> getAllAssetsWithWarnings(Map<String, Object> filters,
                                                                   List<Map<String, String>> orderList,
                                                                   Integer limit,
                                                                   Integer offset) {
        List<Map<String, String>> list = QueryTool.filterOrderList(orderList, "assets", "weather_warnings");

        if (list.isEmpty() || metaDataMapper.filterRegisteredColumnsInTables(List.of("weather_warnings"), List.of(list.get(0).get("column"))).isEmpty()) {
            return assetMapper.selectAssetsWithWarnings(
                    QueryTool.formatFilters(filters),
                    list,
                    limit, offset);
        }
        return assetMapper.selectAssetsWithWarningsPuttingWarningsTableMain(
                QueryTool.formatFilters(filters),
                list,
                limit, offset);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<Asset> getAssetById(String id) {
        return assetMapper.selectAssets(
                QueryTool.formatFilters(Map.of("asset_id", id)),
                null, null, null);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<AssetWithWeatherWarnings> getAssetWithWarningsById(String id) {
        return assetMapper.selectAssetsWithWarnings(
                QueryTool.formatFilters(Map.of("asset_id", id)),
                null, null, null);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<Asset> getAllAssetsByAssetHolderId(String ownerId,
                                                   List<Map<String, String>> orderList,
                                                   Integer limit,
                                                   Integer offset) {
        return assetMapper.selectAssets(
                QueryTool.formatFilters(Map.of("asset_owner_id", ownerId)),
                QueryTool.filterOrderList(orderList, "assets"),
                limit, offset);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<AssetWithWeatherWarnings> getAllAssetsWithWarningsByAssetHolderId(String ownerId,
                                                                                  List<Map<String, String>> orderList,
                                                                                  Integer limit,
                                                                                  Integer offset) {
        return assetMapper.selectAssetsWithWarnings(
                QueryTool.formatFilters(Map.of("asset_owner_id", ownerId)),
                QueryTool.filterOrderList(orderList, "assets", "asset_types", "weather_warnings"),
                limit, offset);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<AssetType> getAllAssetTypes(Map<String, Object> filters,
                                            List<Map<String, String>> orderList,
                                            Integer limit,
                                            Integer offset) {
        return assetMapper.selectAssetTypes(
                QueryTool.formatFilters(filters),
                QueryTool.filterOrderList(orderList, "asset_types"),
                limit, offset);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<String> selectAssetIdsByWarningId(@Param("id") Long id) {
        return assetMapper.selectAssetsWithWarnings(
                        QueryTool.formatFilters(Map.of("warning_id", id)),
                        null, null, null)
                .stream()
                .map(aww -> aww.getAsset().getId())
                .toList();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public int countAssetsWithFilter(Map<String, Object> filters) {
        return assetMapper.countAssetsWithWarnings(QueryTool.formatFilters(filters));
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
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

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
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

        List<Long> warningIds = warningMapper.selectWarningIdsByAssetId(assetIds.get(0));
        for (Long warningId : warningIds) {
            Map<String, Object> notification = contactService.formatNotification(warningId, assetIds.get(0));  //异步处理问题，数据库未同步
            contactService.sendEmail(notification);
        }
        return n;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int updateAssetType(AssetType assetType) {
        return assetMapper.updateAssetType(assetType);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int updateAsset(Asset asset) {
        String ownerId = asset.getOwnerId();
        if (asset.getOwnerId() == null || asset.getOwnerId().isBlank()) {
            List<Asset> tmp = assetMapper.selectAssets(
                    QueryTool.formatFilters(Map.of("asset_id", asset.getId())),
                    null, null, null);
            if (tmp.size() != 1) {
                throw new SpExceptions.GetMethodException("Found " + tmp.size() + " assets for asset id " + asset.getId() + " when updating asset");
            }
            ownerId = tmp.get(0).getOwnerId();
        }
        List<AssetHolder> owner = assetHolderMapper.selectAssetHolders(
                QueryTool.formatFilters(Map.of("asset_holder_id", ownerId)),
                null, null, null);
        if (owner.size() != 1) {
            throw new SpExceptions.GetMethodException(owner.size() + " asset holders found for asset id " + asset.getId() + " when updating asset");
        }
        Instant now = Instant.now();
        owner.get(0).setLastModified(now);
        asset.setLastModified(now);

        assetHolderMapper.updateAssetHolder(owner.get(0));

        List<Long> OldWarningIds = warningMapper.selectWarningIdsByAssetId(asset.getId());

        int value = assetMapper.updateAsset(asset);
        if (asset.getLocationAsJson() == null || asset.getLocationAsJson().isBlank()) {
            return value;
        }

        List<Long> warningIds = warningMapper.selectWarningIdsByAssetId(asset.getId());
        if (warningIds.isEmpty()) {
            return value;
        }
        for (Long warningId : warningIds) {
            if (!OldWarningIds.contains(warningId)) {
                Map<String, Object> notification = contactService.formatNotification(warningId, asset.getId());
                contactService.sendEmail(notification);
            }
        }
        return value;

    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteAssetTypeByIDs(String[] ids) {
        int n = assetMapper.deleteAssetTypeByIDs(ids);
        metaDataMapper.increaseTotalCountByTableName("asset_types", -n);
        return n;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteAssetTypeByIDs(List<String> ids) {
        int n = assetMapper.deleteAssetTypeByIDs(ids);
        metaDataMapper.increaseTotalCountByTableName("asset_types", -n);
        return n;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteAssetByIDs(String[] ids) {
        int n = assetMapper.deleteAssetByIDs(ids);
        metaDataMapper.increaseTotalCountByTableName("assets", -n);
        return n;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteAssetByIDs(List<String> ids) {
        int n = assetMapper.deleteAssetByIDs(ids);
        metaDataMapper.increaseTotalCountByTableName("assets", -n);
        return n;
    }
}
