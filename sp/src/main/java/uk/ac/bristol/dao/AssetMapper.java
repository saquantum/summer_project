package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import uk.ac.bristol.pojo.Asset;
import uk.ac.bristol.pojo.AssetType;
import uk.ac.bristol.pojo.AssetWithWeatherWarnings;

import java.util.List;
import java.util.Map;

@Mapper
public interface AssetMapper {

    List<Asset> selectAllAssets(@Param("orderList") List<Map<String, String>> orderList,
                                @Param("limit") Integer limit,
                                @Param("offset") Integer offset);

    List<AssetWithWeatherWarnings> selectAllAssetsWithWarnings(@Param("orderList") List<Map<String, String>> orderList,
                                                               @Param("limit") Integer limit,
                                                               @Param("offset") Integer offset);

    List<Asset> selectAssetByID(@Param("id") String id);

    List<AssetWithWeatherWarnings> selectAssetWithWarningsByID(@Param("id") String id);

    List<Asset> selectByAsset(Asset asset,
                              @Param("orderList") List<Map<String, String>> orderList,
                              @Param("limit") Integer limit,
                              @Param("offset") Integer offset);

    List<AssetWithWeatherWarnings> selectByAssetWithWarnings(Asset asset,
                                                             @Param("orderList") List<Map<String, String>> orderList,
                                                             @Param("limit") Integer limit,
                                                             @Param("offset") Integer offset);

    List<Asset> selectAllAssetsOfHolder(@Param("ownerId") String ownerId,
                                        @Param("orderList") List<Map<String, String>> orderList,
                                        @Param("limit") Integer limit,
                                        @Param("offset") Integer offset);

    List<AssetWithWeatherWarnings> selectAllAssetsWithWarningsOfHolder(@Param("ownerId") String ownerId,
                                                                       @Param("orderList") List<Map<String, String>> orderList,
                                                                       @Param("limit") Integer limit,
                                                                       @Param("offset") Integer offset);

    List<AssetType> selectAllAssetTypes(@Param("orderList") List<Map<String, String>> orderList,
                                        @Param("limit") Integer limit,
                                        @Param("offset") Integer offset);

    int insertAssetType(AssetType assetType);

    int insertAssetTypeAutoId(AssetType assetType);

    int insertAsset(Asset asset);

    int insertAssetAutoId(Asset asset);

    int updateAssetType(AssetType assetType);

    int updateAsset(Asset asset);

    int deleteAssetTypeByIDs(@Param("ids") String[] ids);

    int deleteAssetTypeByIDs(@Param("ids") List<String> ids);

    int deleteAssetByIDs(@Param("ids") String[] ids);

    int deleteAssetByIDs(@Param("ids") List<String> ids);

    String selectAssetTypeByID(@Param("id") String id);

    String selectAssetOwnerIdByAssetId(@Param("id") String id);
}
