package uk.ac.bristol.service;

import uk.ac.bristol.pojo.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SqlService {
    List<Warning> selectAllWarnings();

    List<UserAsAssetHolder> selectAllAssetHolders();

    List<AssetHolder> selectAssetHolderByID(@Param("id") Integer id);

    List<AssetHolder> selectByAssetHolder(AssetHolder assetHolder);

    int insertAssetHolder(AssetHolder assetHolder);

    int updateAssetHolder(AssetHolder assetHolder);

    int deleteByAssetHolder(AssetHolder assetHolder);

    int deleteByAssetHolderIDs(@Param("ids") int[] ids);
    int deleteByAssetHolderIDs(@Param("ids") List<Integer> ids);

    List<AssetWithWeatherWarnings> selectAllAssets();

    List<AssetWithWeatherWarnings> selectAssetByID(@Param("id") Integer id);

    List<AssetWithWeatherWarnings> selectByAsset(Asset asset);

    List<AssetWithWeatherWarnings> selectAllAssetsOfHolder(@Param("id") Integer id);

    int insertAsset(Asset asset);

    int updateAsset(Asset asset);

    int deleteByAsset(Asset asset);

    int deleteByAssetIDs(@Param("ids") int[] ids);
    int deleteByAssetIDs(@Param("ids") List<Integer> ids);

    User login(User user);
}
