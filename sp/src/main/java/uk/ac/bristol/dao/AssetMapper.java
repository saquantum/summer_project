package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import uk.ac.bristol.pojo.*;

import java.util.List;

@Mapper
public interface AssetMapper {

    List<AssetWithWeatherWarnings> selectAllAssets();

    List<AssetWithWeatherWarnings> selectAssetByID(@Param("id") Long id);

    List<AssetWithWeatherWarnings> selectByAsset(Asset asset);

    List<AssetWithWeatherWarnings> selectAllAssetsOfHolder(@Param("id") Long id);

    long insertAsset(Asset asset);

    long updateAsset(Asset asset);

    long deleteByAssetIDs(@Param("ids") long[] ids);

    long deleteByAssetIDs(@Param("ids") List<Long> ids);
}
