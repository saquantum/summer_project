package com.ourrainwater.dao;

import com.ourrainwater.pojo.Asset;
import com.ourrainwater.pojo.AssetHolder;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SqlMapper {

     List<AssetHolder> selectAllAssetHolders();

     List<AssetHolder> selectAssetHolderByID(@Param("id") Integer id);

     List<AssetHolder> selectByAssetHolder(AssetHolder assetHolder);

     int insertAssetHolder(AssetHolder assetHolder);

     int updateAssetHolder(AssetHolder assetHolder);

     int deleteByAssetHolderIDs(@Param("ids") int[] ids);
     int deleteByAssetHolderIDs(@Param("ids") List<Integer> ids);

     List<Asset> selectAllAssets();

     List<Asset> selectAssetByID(@Param("id") Integer id);

     List<Asset> selectByAsset(Asset asset);

     int insertAsset(Asset asset);

     int updateAsset(Asset asset);

     int deleteByAssetIDs(@Param("ids") int[] ids);
     int deleteByAssetIDs(@Param("ids") List<Integer> ids);
}
