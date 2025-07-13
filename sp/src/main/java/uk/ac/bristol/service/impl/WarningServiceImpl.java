package uk.ac.bristol.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.*;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.Template;
import uk.ac.bristol.pojo.Warning;
import uk.ac.bristol.service.WarningService;
import uk.ac.bristol.util.QueryTool;

import java.util.List;
import java.util.Map;

@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
@Service
public class WarningServiceImpl implements WarningService {

    private final MetaDataMapper metaDataMapper;
    private final WarningMapper warningMapper;
    private final ContactMapper contactMapper;

    public WarningServiceImpl(MetaDataMapper metaDataMapper, WarningMapper warningMapper, ContactMapper contactMapper) {
        this.metaDataMapper = metaDataMapper;
        this.warningMapper = warningMapper;
        this.contactMapper = contactMapper;
    }

    @Override
    public List<Warning> getAllWarnings(Map<String, Object> filters,
                                        List<Map<String, String>> orderList,
                                        Integer limit,
                                        Integer offset) {
        return warningMapper.selectAllWarnings(
                QueryTool.formatFilters(filters),
                QueryTool.filterOrderList(orderList, "weather_warnings"),
                limit, offset);
    }

    @Override
    public List<Warning> getAllWarningsIncludingOutdated(Map<String, Object> filters,
                                                         List<Map<String, String>> orderList,
                                                         Integer limit,
                                                         Integer offset) {
        return warningMapper.selectAllWarningsIncludingOutdated(
                QueryTool.formatFilters(filters),
                QueryTool.filterOrderList(orderList, "weather_warnings"),
                limit, offset);
    }

    @Override
    public List<Warning> getWarningById(Long id) {
        return warningMapper.selectWarningById(id);
    }

    @Override
    public int insertWarning(Warning warning) {
        int n = warningMapper.insertWarning(warning);
        metaDataMapper.increaseTotalCountByTableName("weather_warnings", n);
        return n;
    }

    @Override
    public int insertWarningsList(List<Warning> warnings) {
        if (warnings.isEmpty()) return 0;
        int sum = 0;
        for (Warning warning : warnings) {
            List<Warning> search = warningMapper.selectWarningById(warning.getId());
            if (search.size() > 1) {
                throw new SpExceptions.SystemException("Found" + search.size() + " weather warning data stored in database for id " + warning.getId());
            } else if (search.size() == 1) {
                warningMapper.updateWarning(warning);
                sum++;
            } else {
                warningMapper.insertWarning(warning);
                sum++;
            }
        }
        metaDataMapper.increaseTotalCountByTableName("weather_warnings", sum);
        return sum;
    }

    @Override
    public int updateWarning(Warning warning) {
        return warningMapper.updateWarning(warning);
    }

    @Override
    public int deleteWarningByIDs(Long[] ids) {
        return warningMapper.deleteWarningByIDs(ids);
    }

    @Override
    public int deleteWarningByIDs(List<Long> ids) {
        int n = warningMapper.deleteWarningByIDs(ids);
        metaDataMapper.increaseTotalCountByTableName("weather_warnings", -n);
        return n;
    }

    @Override
    public List<Template> getAllNotificationTemplates(Map<String, Object> filters,
                                                      List<Map<String, String>> orderList,
                                                      Integer limit,
                                                      Integer offset) {
        return contactMapper.selectAllNotificationTemplates(
                QueryTool.formatFilters(filters),
                QueryTool.filterOrderList(orderList, "templates"),
                limit, offset);
    }

    @Override
    public List<Template> getNotificationTemplateByTypes(Template template) {
        return contactMapper.selectNotificationTemplateByTypes(template);
    }

    @Override
    public List<Template> getNotificationTemplateById(Long id) {
        return contactMapper.selectNotificationTemplateById(id);
    }

    @Override
    public int insertNotificationTemplate(Template templates) {
        int n = contactMapper.insertNotificationTemplate(templates);
        metaDataMapper.increaseTotalCountByTableName("templates", n);
        return n;
    }

    @Override
    public int updateNotificationTemplateMessageById(Template template) {
        return contactMapper.updateNotificationTemplateMessageById(template);
    }

    @Override
    public int updateNotificationTemplateMessageByTypes(Template template) {
        return contactMapper.updateNotificationTemplateMessageByTypes(template);
    }

    @Override
    public int deleteNotificationTemplateByIds(Long[] ids) {
        int n = contactMapper.deleteNotificationTemplateByIds(ids);
        metaDataMapper.increaseTotalCountByTableName("templates", -n);
        return n;
    }

    @Override
    public int deleteNotificationTemplateByIds(List<Long> ids) {
        int n = contactMapper.deleteNotificationTemplateByIds(ids);
        metaDataMapper.increaseTotalCountByTableName("templates", -n);
        return n;
    }

    @Override
    public int deleteNotificationTemplateByType(Template template) {
        int n = contactMapper.deleteNotificationTemplateByType(template);
        metaDataMapper.increaseTotalCountByTableName("templates", -n);
        return n;
    }

    @Override
    public Map<String, Object> getUserInboxMessagesByUserId(String userId) {
        return contactMapper.selectUserInboxMessagesByUserId(userId);
    }

    @Override
    public int insertInboxMessageToUser(Map<String, Object> message) {
        return contactMapper.insertInboxMessageToUser(message);
    }

    @Override
    public int updateInboxMessageByUserId(Map<String, Object> message) {
        return contactMapper.updateInboxMessageByUserId(message);
    }

    @Override
    public int deleteInboxMessageByFilter(Map<String, Object> filters) {
        return contactMapper.deleteInboxMessageByFilter(QueryTool.formatFilters(filters));
    }

    @Override
    public int deleteOutDatedInboxMessages() {
        return contactMapper.deleteOutDatedInboxMessages();
    }

    @Override
    public int deleteOutDatedInboxMessagesByUserId(String userId) {
        return contactMapper.deleteOutDatedInboxMessagesByUserId(userId);
    }

    @Override
    public boolean testWarningIdExists(Long warningId) {
        return warningMapper.testWarningExists(warningId);
    }

    @Override
    public List<Long> selectWarningIdsByAssetId(String id){
        return warningMapper.selectWarningIdsByAssetId(id);
    }
}
