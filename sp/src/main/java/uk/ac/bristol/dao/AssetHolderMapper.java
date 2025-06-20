package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import uk.ac.bristol.pojo.*;

import java.util.List;

@Mapper
public interface AssetHolderMapper {

    List<UserAsAssetHolder> selectAllAssetHolders();

    List<AssetHolder> selectAssetHolderByID(@Param("id") Integer id);

    List<AssetHolder> selectByAssetHolder(AssetHolder assetHolder);

    int insertAssetHolder(AssetHolder assetHolder);

    int updateAssetHolder(AssetHolder assetHolder);

    int deleteByAssetHolderIDs(@Param("ids") int[] ids);

    int deleteByAssetHolderIDs(@Param("ids") List<Integer> ids);
}
