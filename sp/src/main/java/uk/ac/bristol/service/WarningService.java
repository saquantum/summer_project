package uk.ac.bristol.service;

import uk.ac.bristol.pojo.Templates;
import uk.ac.bristol.pojo.Warning;

import java.util.List;
import java.util.Map;

public interface WarningService {

    List<Warning> getAllWarnings(List<Map<String, String>> orderList,
                                 Integer limit,
                                 Integer offset);

    List<Warning> getAllWarningsIncludingOutdated(List<Map<String, String>> orderList,
                                                  Integer limit,
                                                  Integer offset);

    List<Warning> getWarningById(Long id);

    int insertWarning(Warning warning);

    int updateWarning(Warning warning);

    int deleteWarningByIDs(Long[] ids);

    int deleteWarningByIDs(List<Long> ids);

    List<Map<String, Object>> getAllNotificationTemplates();

    int insertNotificationTemplate(Templates templates);

    int updateNotificationTemplate(Map<String, String> template);

    int deleteNotificationTemplateByIds(Integer[] ids);

    int deleteNotificationTemplateByIds(List<Integer> ids);

    String getMessageByInfo(String assetType, String weatherType, String severity);

    int updateMessageByInfo(String assetType, String weatherType, String severity, String message);
}
