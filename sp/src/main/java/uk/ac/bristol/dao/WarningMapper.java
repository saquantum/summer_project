package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import uk.ac.bristol.pojo.Template;
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

    List<String> selectOwnerIdOfAllAssetsIntersectingWithGivenWarning(@Param("warningId") Long warningId);

    Boolean testIfGivenAssetIntersectsWithWarning(@Param("assetId") String assetId, @Param("warningId") Long warningId);

    int insertWarning(Warning warning);

    int updateWarning(Warning warning);

    int deleteWarningByIDs(@Param("ids") Long[] ids);

    int deleteWarningByIDs(@Param("ids") List<Long> ids);

    List<Template> selectAllNotificationTemplates(@Param("orderList") List<Map<String, String>> orderList,
                                                  @Param("limit") Integer limit,
                                                  @Param("offset") Integer offset);

    List<Template> selectNotificationTemplateByTypes(Template template);

    List<Template> selectNotificationTemplateById(@Param("id") Long id);

    int insertNotificationTemplate(Template template);

    int updateNotificationTemplateMessageByTypes(Template template);

    int updateNotificationTemplateMessageById(Template template);

    int deleteNotificationTemplateByIds(@Param("ids") Long[] ids);

    int deleteNotificationTemplateByIds(@Param("ids") List<Long> ids);

    int deleteNotificationTemplateByType(Template template);
}
