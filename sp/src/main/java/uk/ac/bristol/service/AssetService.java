package uk.ac.bristol.service;

import org.apache.ibatis.annotations.Param;
import uk.ac.bristol.pojo.Asset;
import uk.ac.bristol.pojo.AssetType;
import uk.ac.bristol.pojo.AssetWithWeatherWarnings;

import java.util.List;
import java.util.Map;

public interface AssetService {

    List<Asset> getAllAssets(List<Map<String, String>> orderList,
                             Integer limit,
                             Integer offset);

    List<AssetWithWeatherWarnings> getAllAssetsWithWarnings(List<Map<String, String>> orderList,
                                                            Integer limit,
                                                            Integer offset);

    List<Asset> getAssetById(String id);

    List<AssetWithWeatherWarnings> getAssetWithWarningsById(String id);

    List<Asset> getAssetByAsset(Asset asset,
                                List<Map<String, String>> orderList,
                                Integer limit,
                                Integer offset);

    List<AssetWithWeatherWarnings> getAssetWithWarningsByAsset(Asset asset,
                                                               List<Map<String, String>> orderList,
                                                               Integer limit,
                                                               Integer offset);

    List<Asset> getAllAssetsByAssetHolderId(String ownerId,
                                            List<Map<String, String>> orderList,
                                            Integer limit,
                                            Integer offset);

    List<AssetWithWeatherWarnings> getAllAssetsWithWarningsByAssetHolderId(String ownerId,
                                                                           List<Map<String, String>> orderList,
                                                                           Integer limit,
                                                                           Integer offset);

    List<AssetType> getAllAssetTypes(List<Map<String, String>> orderList,
                                     Integer limit,
                                     Integer offset);

    int insertAssetType(AssetType assetType);

    int insertAsset(Asset asset);

    int updateAssetType(AssetType assetType);

    int updateAsset(Asset asset);

    int deleteAssetTypeByIDs(@Param("ids") String[] ids);

    int deleteAssetTypeByIDs(@Param("ids") List<String> ids);

    int deleteAssetByIDs(@Param("ids") String[] ids);

    int deleteAssetByIDs(@Param("ids") List<String> ids);
}
