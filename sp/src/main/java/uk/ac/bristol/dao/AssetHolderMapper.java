package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import uk.ac.bristol.pojo.*;

import java.util.List;

@Mapper
public interface AssetHolderMapper {

    List<UserAsAssetHolder> selectAllAssetHolders();

    List<AssetHolder> selectAssetHolderByID(@Param("id") Long id);

    List<AssetHolder> selectByAssetHolder(AssetHolder assetHolder);

    long insertAssetHolder(AssetHolder assetHolder);

    long updateAssetHolder(AssetHolder assetHolder);

    long deleteByAssetHolderIDs(@Param("ids") long[] ids);

    long deleteByAssetHolderIDs(@Param("ids") List<Long> ids);
}
