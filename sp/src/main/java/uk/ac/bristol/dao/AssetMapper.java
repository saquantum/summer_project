package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import uk.ac.bristol.pojo.*;

import java.util.List;

@Mapper
public interface AssetMapper {

    List<Asset> selectAllAssets();

    List<AssetWithWeatherWarnings> selectAllAssetsWithWarnings();

    List<Asset> selectAssetByID(@Param("id") String id);

    List<AssetWithWeatherWarnings> selectAssetWithWarningsByID(@Param("id") String id);

    List<Asset> selectByAsset(Asset asset);

    List<AssetWithWeatherWarnings> selectByAssetWithWarnings(Asset asset);

    List<Asset> selectAllAssetsOfHolder(@Param("ownerId") String ownerId);

    List<AssetWithWeatherWarnings> selectAllAssetsWithWarningsOfHolder(@Param("ownerId") String ownerId);

    List<AssetType> selectAllAssetTypes();

    int insertAssetType(AssetType assetType);

    int insertAsset(Asset asset);

    int updateAssetType(AssetType assetType);

    int updateAsset(Asset asset);

    int deleteAssetTypeByIDs(@Param("ids") String[] ids);

    int deleteAssetTypeByIDs(@Param("ids") List<String> ids);

    int deleteAssetByIDs(@Param("ids") String[] ids);

    int deleteAssetByIDs(@Param("ids") List<String> ids);
}
