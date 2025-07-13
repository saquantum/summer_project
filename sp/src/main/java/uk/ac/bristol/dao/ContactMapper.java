package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import uk.ac.bristol.pojo.Template;

import java.util.List;
import java.util.Map;

@Mapper
public interface ContactMapper {

    List<Template> selectAllNotificationTemplates(@Param("filterString") String filterString,
                                                  @Param("orderList") List<Map<String, String>> orderList,
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

    Map<String, Object> selectUserInboxMessagesByUserId(@Param("userId") String userId);

    int insertInboxMessageToUser(Map<String, Object> message);

    int updateInboxMessageByUserId(Map<String, Object> message);

    int deleteInboxMessageByFilter(@Param("filterString") String filterString);

    int deleteOutDatedInboxMessages();

    int deleteOutDatedInboxMessagesByUserId(@Param("userId") String userId);
}
