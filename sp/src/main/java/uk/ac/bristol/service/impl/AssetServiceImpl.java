package uk.ac.bristol.service.impl;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.AssetMapper;
import uk.ac.bristol.dao.MetaDataMapper;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.*;
import uk.ac.bristol.service.AssetService;
import uk.ac.bristol.service.ContactService;
import uk.ac.bristol.service.UserService;
import uk.ac.bristol.service.WarningService;
import uk.ac.bristol.util.QueryTool;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AssetServiceImpl implements AssetService {

    private final MetaDataMapper metaDataMapper;
    private final AssetMapper assetMapper;
    private final UserService userService;
    private final WarningService warningService;
    private final ContactService contactService;

    public AssetServiceImpl(MetaDataMapper metaDataMapper, AssetMapper assetMapper, UserService userService, WarningService warningService, ContactService contactService) {
        this.metaDataMapper = metaDataMapper;
        this.assetMapper = assetMapper;
        this.userService = userService;
        this.warningService = warningService;
        this.contactService = contactService;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<Asset> getAssets(Map<String, Object> filters,
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
    public List<AssetWithWeatherWarnings> getAssetsWithWarnings(Map<String, Object> filters,
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
    public Asset getAssetById(String id) {
        List<Asset> asset = assetMapper.selectAssets(
                QueryTool.formatFilters(Map.of("asset_id", id)),
                null, null, null);
        if (asset.size() != 1) {
            throw new SpExceptions.GetMethodException("Get " + asset.size() + " assets for asset id " + id);
        }
        return asset.get(0);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public AssetWithWeatherWarnings getAssetWithWarningsById(String id) {
        List<AssetWithWeatherWarnings> asset = assetMapper.selectAssetsWithWarnings(
                QueryTool.formatFilters(Map.of("asset_id", id)),
                null, null, null);
        if (asset.size() != 1) {
            throw new SpExceptions.GetMethodException("Get " + asset.size() + " assets for asset id " + id);
        }
        return asset.get(0);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<Asset> getAssetsByOwnerId(String ownerId,
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
    public List<AssetWithWeatherWarnings> getAssetsWithWarningsByOwnerId(String ownerId,
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
    public List<AssetType> getAssetTypes(Map<String, Object> filters,
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
    public List<String> getAssetIdsIntersectingWithGivenWarning(@Param("id") Long id) {
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

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public boolean compareAssetLastModified(String assetId, Long timestamp) {
        Asset asset = getAssetById(assetId);
        return !asset.getLastModified().isAfter(Instant.ofEpochMilli(timestamp));
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
        if (asset.getId() == null || asset.getId().isEmpty()) {
            List<String> assetId = assetMapper.insertAssetReturningId(asset);
            n = assetId.size();
            if (n != 1) {
                throw new SpExceptions.GetMethodException("Inserted " + n + " assets with returning auto id mode");
            }

            // if the new asset intersects with live warnings, send notifications
            asset.setId(assetId.get(0));
            List<Warning> warnings = warningService.getWarningsIntersectingWithGivenAsset(asset.getId());
            if (!warnings.isEmpty()) {
                User owner = userService.getUserByUserId(asset.getOwnerId());
                for (Warning warning : warnings) {
                    contactService.sendNotificationsToUser(warning, new UserWithAssets(owner, new ArrayList<>(List.of(asset))));
                }
            }
        } else {
            n = assetMapper.insertAsset(asset);
        }

        metaDataMapper.increaseTotalCountByTableName("assets", n);
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
        // get owner of the asset
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
        User owner = userService.getUserByUserId(ownerId);

        // update user last modified
        Instant now = Instant.now();
        owner.setLastModified(now);
        userService.updateUser(owner);

        // if asset area is not touched, update now and return early
        asset.setLastModified(now);
        if (asset.getLocationAsJson() == null || asset.getLocationAsJson().isBlank()) {
            return assetMapper.updateAsset(asset);
        }

        // asset area is touched, get old intersecting warnings for the record
        Map<Long, Warning> oldWarnings = warningService
                .getWarningsIntersectingWithGivenAsset(asset.getId())
                .stream()
                .collect(Collectors.toMap(Warning::getId, warning -> warning));

        int n = assetMapper.updateAsset(asset);

        // if area is touched but no intersecting live warning, return early
        List<Warning> newWarnings = warningService.getWarningsIntersectingWithGivenAsset(asset.getId());
        if (newWarnings.isEmpty()) {
            return n;
        }

        // has intersecting live warnings, compare them with old warnings
        for (Warning warning : newWarnings) {
            // if new warning is one of the old warnings, do nothing
            if (oldWarnings.containsKey(warning.getId())) {
                continue;
            }
            // new warning is new, send notifications
            contactService.sendNotificationsToUser(warning, new UserWithAssets(owner, new ArrayList<>(List.of(asset))));
        }
        return n;
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
