package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import uk.ac.bristol.pojo.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface AssetHolderMapper {

    List<Map<String, String>> selectAddressByAssetHolderId(@Param("assetHolderId") String assetHolderId);

    int insertAddress(Map<String, String> map);

    int updateAddressByAssetHolderId(Map<String, String> map);

    int deleteAddressByAssetHolderId(@Param("assetHolderId") String assetHolderId);

    List<Map<String, Object>> selectContactPreferencesByAssetHolderId(@Param("assetHolderId") String assetHolderId);

    int insertContactPreferences(Map<String, Object> map);

    int updateContactPreferencesByAssetHolderId(Map<String, Object> map);

    int deleteContactPreferencesByAssetHolderId(@Param("assetHolderId") String assetHolderId);

    List<AssetHolder> selectAllAssetHolders();

    List<AssetHolder> selectAssetHolderByID(@Param("id") String id);

    List<AssetHolder> selectByAssetHolder(AssetHolder assetHolder);

    int insertAssetHolder(AssetHolder assetHolder);

    int updateAssetHolder(AssetHolder assetHolder);

    int deleteAssetHolderByAssetHolderIDs(@Param("ids") String[] ids);

    int deleteAssetHolderByAssetHolderIDs(@Param("ids") List<String> ids);
}
