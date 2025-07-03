package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import uk.ac.bristol.pojo.AssetHolder;

import java.util.List;
import java.util.Map;

@Mapper
public interface AssetHolderMapper {

    List<Map<String, String>> selectAddressByAssetHolderId(@Param("assetHolderId") String assetHolderId);

    int insertAddress(Map<String, String> map);

    int updateAddressByAssetHolderId(Map<String, String> map);

    int deleteAddressByAssetHolderIds(@Param("ids") String[] ids);

    int deleteAddressByAssetHolderIds(@Param("ids") List<String> ids);

    List<Map<String, Object>> selectContactPreferencesByAssetHolderId(@Param("assetHolderId") String assetHolderId);

    int insertContactPreferences(Map<String, Object> map);

    int updateContactPreferencesByAssetHolderId(Map<String, Object> map);

    int deleteContactPreferencesByAssetHolderIds(@Param("ids") String[] ids);

    int deleteContactPreferencesByAssetHolderIds(@Param("ids") List<String> ids);

    List<AssetHolder> selectAllAssetHolders(@Param("orderList") List<Map<String, String>> orderList,
                                            @Param("limit") Integer limit,
                                            @Param("offset") Integer offset);

    List<AssetHolder> selectAssetHolderByIDs(@Param("ids") List<String> ids,
                                             @Param("orderList") List<Map<String, String>> orderList,
                                             @Param("limit") Integer limit,
                                             @Param("offset") Integer offset);

    List<AssetHolder> selectByAssetHolder(AssetHolder assetHolder);

    Boolean testEmailAddressExistence(@Param("email") String email);

    List<String> selectAssetHolderIdByEmail(@Param("email") String email);

    int insertAssetHolder(AssetHolder assetHolder);

    int insertAssetHolderAutoId(AssetHolder assetHolder);

    String generateAssetHolderId(AssetHolder assetHolder);

    int updateAssetHolder(AssetHolder assetHolder);

    int deleteAssetHolderByAssetHolderIDs(@Param("ids") String[] ids);

    int deleteAssetHolderByAssetHolderIDs(@Param("ids") List<String> ids);
}
