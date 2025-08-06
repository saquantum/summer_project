package uk.ac.bristol.service;

import uk.ac.bristol.pojo.Asset;
import uk.ac.bristol.pojo.AssetType;
import uk.ac.bristol.pojo.AssetWithWeatherWarnings;

import java.util.List;
import java.util.Map;

public interface AssetService {

    List<Asset> getAssets(Map<String, Object> filters,
                          List<Map<String, String>> orderList,
                          Integer limit,
                          Integer offset);

    List<AssetWithWeatherWarnings> getAssetsWithWarnings(Map<String, Object> filters,
                                                         List<Map<String, String>> orderList,
                                                         Integer limit,
                                                         Integer offset);

    List<AssetWithWeatherWarnings> getCursoredAssetsWithWarnings(Long lastAssetRowId,
                                                                 Map<String, Object> filters,
                                                                 List<Map<String, String>> orderList,
                                                                 Integer limit,
                                                                 Integer offset);

    Asset getAssetById(String id);

    AssetWithWeatherWarnings getAssetWithWarningsById(String id);

    List<Asset> getAssetsByOwnerId(String ownerId,
                                   List<Map<String, String>> orderList,
                                   Integer limit,
                                   Integer offset);

    List<AssetWithWeatherWarnings> getAssetsWithWarningsByOwnerId(String ownerId,
                                                                  List<Map<String, String>> orderList,
                                                                  Integer limit,
                                                                  Integer offset);

    List<AssetType> getAssetTypes(Map<String, Object> filters,
                                  List<Map<String, String>> orderList,
                                  Integer limit,
                                  Integer offset);

    List<AssetType> getCursoredAssetTypes(Long lastAssetTypeRowId,
                                          Map<String, Object> filters,
                                          List<Map<String, String>> orderList,
                                          Integer limit,
                                          Integer offset);

    List<String> getAssetIdsIntersectingWithGivenWarning(Long id);

    Map<String, Integer> groupAssetLocationByCountry(Map<String, Object> filters);

    Map<String, Integer> groupAssetLocationByRegion(Map<String, Object> filters);

    Map<String, Integer> groupAssetLocationByAdminDistrict(Map<String, Object> filters);

    int countAssetsWithFilter(Map<String, Object> filters);

    boolean compareAssetLastModified(String assetId, Long timestamp);

    int insertAssetType(AssetType assetType);

    int insertAsset(Asset asset);

    String insertAssetReturningId(Asset asset);

    int updateAssetType(AssetType assetType);

    int updateAsset(Asset asset);

    int deleteAssetTypeByIDs(String[] ids);

    int deleteAssetTypeByIDs(List<String> ids);

    int deleteAssetByIDs(String[] ids);

    int deleteAssetByIDs(List<String> ids);

    boolean upsertAssetPostcodeByAssetId(String assetId, Map<String, Object> map);
}
