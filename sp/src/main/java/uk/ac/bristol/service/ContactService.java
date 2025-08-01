package uk.ac.bristol.service;

import uk.ac.bristol.controller.ResponseBody;
import uk.ac.bristol.pojo.Template;
import uk.ac.bristol.pojo.UserWithAssets;
import uk.ac.bristol.pojo.Warning;

import java.util.List;
import java.util.Map;

public interface ContactService {

    void sendNotificationsToUser(Warning warning, UserWithAssets uwa);

    Map<String, Object> formatNotification(Warning warning, UserWithAssets uwa, String channel);

    ResponseBody unsubscribeEmail(String token);

    void sendSms(String toPhoneNumber, String messageBody);

    ResponseBody generateCode(String email);

    ResponseBody validateCode(String email, String code);

    ResponseBody registerGenerateCode(String email);

    List<Template> getNotificationTemplates(Map<String, Object> filters,
                                            List<Map<String, String>> orderList,
                                            Integer limit,
                                            Integer offset);

    List<Template> getCursoredNotificationTemplates(Long lastTemplateId,
                                                    Map<String, Object> filters,
                                                    List<Map<String, String>> orderList,
                                                    Integer limit,
                                                    Integer offset);

    List<Template> getNotificationTemplateByTypes(Template template);

    Template getNotificationTemplateById(Long id);

    int insertNotificationTemplate(Template templates);

    int updateNotificationTemplateMessageById(Template template);

    int updateNotificationTemplateMessageByTypes(Template template);

    int deleteNotificationTemplateByIds(Long[] ids);

    int deleteNotificationTemplateByIds(List<Long> ids);

    int deleteNotificationTemplateByType(Template template);

    List<Map<String, Object>> getUserInboxMessagesByUserId(String userId);

    int insertInboxMessageToUser(Map<String, Object> message);

    int insertInboxMessageToUsersByFilter(Map<String, Object> filters, Map<String, Object> message);

    int updateInboxMessageByUserId(Map<String, Object> message);

    int deleteInboxMessageByFilter(Map<String, Object> filters);

    int deleteOutDatedInboxMessages();

    int deleteOutDatedInboxMessagesByUserId(String userId);
}
