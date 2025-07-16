package uk.ac.bristol.service;

import uk.ac.bristol.pojo.Template;
import uk.ac.bristol.pojo.Warning;

import java.util.List;
import java.util.Map;

public interface WarningService {

    List<Warning> getAllWarnings(Map<String, Object> filters,
                                 List<Map<String, String>> orderList,
                                 Integer limit,
                                 Integer offset);

    List<Warning> getAllWarningsIncludingOutdated(Map<String, Object> filters,
                                                  List<Map<String, String>> orderList,
                                                  Integer limit,
                                                  Integer offset);

    List<Warning> getWarningById(Long id);

    boolean testWarningIdExists(Long warningId);

    List<Long> selectWarningIdsByAssetId(String id);

    void storeWarningsAndSendNotifications(List<Warning> parsedWarnings);

    int insertWarning(Warning warning);

    int insertWarningsList(List<Warning> warnings);

    int updateWarning(Warning warning);

    int deleteWarningByIDs(Long[] ids);

    int deleteWarningByIDs(List<Long> ids);

    List<Template> getAllNotificationTemplates(Map<String, Object> filters,
                                               List<Map<String, String>> orderList,
                                               Integer limit,
                                               Integer offset);

    List<Template> getNotificationTemplateByTypes(Template template);

    List<Template> getNotificationTemplateById(Long id);

    int insertNotificationTemplate(Template templates);

    int updateNotificationTemplateMessageById(Template template);

    int updateNotificationTemplateMessageByTypes(Template template);

    int deleteNotificationTemplateByIds(Long[] ids);

    int deleteNotificationTemplateByIds(List<Long> ids);

    int deleteNotificationTemplateByType(Template template);


    Map<String, Object> getUserInboxMessagesByUserId(String userId);

    int insertInboxMessageToUser(Map<String, Object> message);

    int updateInboxMessageByUserId(Map<String, Object> message);

    int deleteInboxMessageByFilter(Map<String, Object> filters);

    int deleteOutDatedInboxMessages();

    int deleteOutDatedInboxMessagesByUserId(String userId);
}
