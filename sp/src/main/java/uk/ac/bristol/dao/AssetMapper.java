package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import uk.ac.bristol.pojo.Asset;
import uk.ac.bristol.pojo.AssetType;
import uk.ac.bristol.pojo.AssetWithWeatherWarnings;
import uk.ac.bristol.pojo.FilterItemDTO;

import java.util.List;
import java.util.Map;

@Mapper
public interface AssetMapper {

    List<Asset> selectAssets(@Param("filterList") List<FilterItemDTO> filterList,
                             @Param("orderList") List<Map<String, String>> orderList,
                             @Param("limit") Integer limit,
                             @Param("offset") Integer offset);

    List<AssetWithWeatherWarnings> selectAssetsWithWarnings(@Param("filterList") List<FilterItemDTO> filterList,
                                                            @Param("orderList") List<Map<String, String>> orderList,
                                                            @Param("limit") Integer limit,
                                                            @Param("offset") Integer offset);

    List<AssetWithWeatherWarnings> selectAssetsWithWarningsPuttingWarningsTableMain(@Param("filterList") List<FilterItemDTO> filterList,
                                                                                    @Param("orderList") List<Map<String, String>> orderList,
                                                                                    @Param("limit") Integer limit,
                                                                                    @Param("offset") Integer offset);

    int countAssetsWithWarnings(@Param("filterList") List<FilterItemDTO> filterList);

    int insertAsset(Asset asset);

    List<String> insertAssetReturningId(Asset asset);

    int insertAssetAutoId(Asset asset);

    int updateAsset(Asset asset);

    int deleteAssetByIDs(@Param("ids") String[] ids);

    int deleteAssetByIDs(@Param("ids") List<String> ids);

    /* asset types */

    List<AssetType> selectAssetTypes(@Param("filterList") List<FilterItemDTO> filterList,
                                     @Param("orderList") List<Map<String, String>> orderList,
                                     @Param("limit") Integer limit,
                                     @Param("offset") Integer offset);

    int insertAssetType(AssetType assetType);

    int insertAssetTypeAutoId(AssetType assetType);

    int updateAssetType(AssetType assetType);

    int deleteAssetTypeByIDs(@Param("ids") String[] ids);

    int deleteAssetTypeByIDs(@Param("ids") List<String> ids);
}
