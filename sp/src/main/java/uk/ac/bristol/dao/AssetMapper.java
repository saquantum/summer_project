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

    List<Asset> selectAssets(@Param("filterString") String filterString,
                             @Param("orderList") List<Map<String, String>> orderList,
                             @Param("limit") Integer limit,
                             @Param("offset") Integer offset);

    List<AssetWithWeatherWarnings> selectAssetsWithWarnings(@Param("filterString") String filterString,
                                                            @Param("orderList") List<Map<String, String>> orderList,
                                                            @Param("limit") Integer limit,
                                                            @Param("offset") Integer offset);

    List<AssetWithWeatherWarnings> selectAssetsWithWarningsPuttingWarningsTableMain(@Param("filterString") String filterString,
                                                                                    @Param("orderList") List<Map<String, String>> orderList,
                                                                                    @Param("limit") Integer limit,
                                                                                    @Param("offset") Integer offset);

    int countAssetsWithWarnings(@Param("filterString") String filterString);

    List<AssetType> selectAssetTypes(@Param("filterString") String filterString,
                                     @Param("orderList") List<Map<String, String>> orderList,
                                     @Param("limit") Integer limit,
                                     @Param("offset") Integer offset);

    int insertAssetType(AssetType assetType);

    int insertAssetTypeAutoId(AssetType assetType);

    int insertAsset(Asset asset);

    List<String> insertAssetReturningId(Asset asset);

    int insertAssetAutoId(Asset asset);

    int updateAssetType(AssetType assetType);

    int updateAsset(Asset asset);

    int deleteAssetTypeByIDs(@Param("ids") String[] ids);

    int deleteAssetTypeByIDs(@Param("ids") List<String> ids);

    int deleteAssetByIDs(@Param("ids") String[] ids);

    int deleteAssetByIDs(@Param("ids") List<String> ids);
}
