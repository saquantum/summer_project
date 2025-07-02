package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import uk.ac.bristol.pojo.Templates;
import uk.ac.bristol.pojo.Warning;

import java.util.List;
import java.util.Map;

@Mapper
public interface WarningMapper {

    List<Warning> selectAllWarnings(@Param("orderList") List<Map<String, String>> orderList,
                                    @Param("limit") Integer limit,
                                    @Param("offset") Integer offset);

    List<Warning> selectAllWarningsIncludingOutdated(@Param("orderList") List<Map<String, String>> orderList,
                                                     @Param("limit") Integer limit,
                                                     @Param("offset") Integer offset);

    List<Warning> selectWarningById(@Param("id") Long id);

    int insertWarning(Warning warning);

    int updateWarning(Warning warning);

    int deleteWarningByIDs(@Param("ids") Long[] ids);

    int deleteWarningByIDs(@Param("ids") List<Long> ids);

    List<Map<String, Object>> selectAllNotificationTemplates();

    int insertNotificationTemplate(Templates templates);

    int updateNotificationTemplate(Map<String, String> template);

    int deleteNotificationTemplateByIds(@Param("ids") Integer[] ids);

    int deleteNotificationTemplateByIds(@Param("ids") List<Integer> ids);

    List<String> selectOwnerIdOfAllAssetsIntersectingWithGivenWarning(@Param("warningId") Long warningId);

    Boolean testIfGivenAssetIntersectsWithWarning(@Param("assetId") String assetId, @Param("warningId") Long warningId);

    String selectMessageByInfo(@Param("assetType") String assetType, @Param("weatherType") String weatherType, @Param("severity") String severity);

    int updateMessageByInfo(@Param("assetType") String assetType, @Param("weatherType") String weatherType, @Param("severity") String severity, @Param("message") String message);
}
