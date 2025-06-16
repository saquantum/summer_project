package uk.ac.bristol.dao;

import uk.ac.bristol.pojo.*;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SqlMapper {

    List<UserAsAssetHolder> selectUserByAssetHolderId(@Param("id") Integer id);

    List<Warning> selectAllWarnings();

    List<UserAsAssetHolder> selectAllAssetHolders();

    List<AssetHolder> selectAssetHolderByID(@Param("id") Integer id);

    List<AssetHolder> selectByAssetHolder(AssetHolder assetHolder);

    int insertAssetHolder(AssetHolder assetHolder);

    int updateAssetHolder(AssetHolder assetHolder);

    int deleteByAssetHolderIDs(@Param("ids") int[] ids);

    int deleteByAssetHolderIDs(@Param("ids") List<Integer> ids);

    List<AssetWithWeatherWarnings> selectAllAssets();

    List<AssetWithWeatherWarnings> selectAssetByID(@Param("id") Integer id);

    List<AssetWithWeatherWarnings> selectByAsset(Asset asset);

    List<AssetWithWeatherWarnings> selectAllAssetsOfHolder(@Param("id") Integer id);

    int insertAsset(Asset asset);

    int updateAsset(Asset asset);

    int deleteByAssetIDs(@Param("ids") int[] ids);

    int deleteByAssetIDs(@Param("ids") List<Integer> ids);

    List<User> loginQuery(User user);
}
