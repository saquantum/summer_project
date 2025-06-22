package uk.ac.bristol.service;

import org.apache.ibatis.annotations.Param;
import uk.ac.bristol.pojo.Asset;
import uk.ac.bristol.pojo.AssetType;
import uk.ac.bristol.pojo.AssetWithWeatherWarnings;

import java.util.List;

public interface AssetService {

    List<Asset> getAllAssets();

    List<AssetWithWeatherWarnings> getAllAssetsWithWarnings();

    List<Asset> getAssetById(String id);

    List<AssetWithWeatherWarnings> getAssetWithWarningsById(String id);

    List<Asset> getAssetByAsset(Asset asset);

    List<AssetWithWeatherWarnings> getAssetWithWarningsByAsset(Asset asset);

    List<Asset> getAllAssetsByAssetHolderId(String ownerId);

    List<AssetWithWeatherWarnings> getAllAssetsWithWarningsByAssetHolderId(String ownerId);

    List<AssetType> getAllAssetTypes();

    int insertAssetType(AssetType assetType);

    int insertAsset(Asset asset);

    int updateAssetType(AssetType assetType);

    int updateAsset(Asset asset);

    int deleteAssetTypeByIDs(@Param("ids") String[] ids);

    int deleteAssetTypeByIDs(@Param("ids") List<String> ids);

    int deleteAssetByIDs(@Param("ids") String[] ids);

    int deleteAssetByIDs(@Param("ids") List<String> ids);
}
