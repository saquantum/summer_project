package uk.ac.bristol.service;

import uk.ac.bristol.pojo.Asset;
import uk.ac.bristol.pojo.AssetHolder;
import org.apache.ibatis.annotations.Param;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.pojo.UserAsAssetHolder;

import java.util.List;

public interface SqlService {
    List<UserAsAssetHolder> selectAllAssetHolders();

    List<AssetHolder> selectAssetHolderByID(@Param("id") Integer id);

    List<AssetHolder> selectByAssetHolder(AssetHolder assetHolder);

    int insertAssetHolder(AssetHolder assetHolder);

    int updateAssetHolder(AssetHolder assetHolder);

    int deleteByAssetHolder(AssetHolder assetHolder);

    int deleteByAssetHolderIDs(@Param("ids") int[] ids);
    int deleteByAssetHolderIDs(@Param("ids") List<Integer> ids);

    List<Asset> selectAllAssets();

    List<Asset> selectAssetByID(@Param("id") Integer id);

    List<Asset> selectByAsset(Asset asset);

    List<Asset> selectAllAssetsOfHolder(@Param("id") Integer id);

    int insertAsset(Asset asset);

    int updateAsset(Asset asset);

    int deleteByAsset(Asset asset);

    int deleteByAssetIDs(@Param("ids") int[] ids);
    int deleteByAssetIDs(@Param("ids") List<Integer> ids);

    User login(User user);
}
