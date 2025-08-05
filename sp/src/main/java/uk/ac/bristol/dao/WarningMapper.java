package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import uk.ac.bristol.pojo.FilterItemDTO;
import uk.ac.bristol.pojo.Warning;

import java.util.List;
import java.util.Map;

@Mapper
public interface WarningMapper {

    List<Warning> selectWarnings(@Param("filterList") List<FilterItemDTO> filterList,
                                 @Param("orderList") List<Map<String, String>> orderList,
                                 @Param("limit") Integer limit,
                                 @Param("offset") Integer offset);

    List<Map<String, Object>> selectWarningAnchor(@Param("rowId") Long rowId);

    List<Warning> selectWarningsIncludingOutdated(@Param("filterList") List<FilterItemDTO> filterList,
                                                  @Param("orderList") List<Map<String, String>> orderList,
                                                  @Param("limit") Integer limit,
                                                  @Param("offset") Integer offset);

    List<Map<String, Object>> selectWarningsIncludingOutdatedAnchor(@Param("rowId") Long rowId);

    List<Warning> selectWarningById(@Param("id") Long id);

    List<Warning> selectWarningsIntersectingWithGivenAsset(@Param("assetId") String assetId);

    boolean testWarningExistence(@Param("id") Long id);

    boolean testWarningDetailDiff(Warning warning);

    boolean testWarningAreaDiff(@Param("id") Long warningId, @Param("areaAsJson") String areaAsJson);

    int insertWarning(Warning warning);

    int updateWarning(Warning warning);

    int deleteWarningByIDs(@Param("ids") Long[] ids);

    int deleteWarningByIDs(@Param("ids") List<Long> ids);
}