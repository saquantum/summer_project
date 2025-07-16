package uk.ac.bristol.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.ContactMapper;
import uk.ac.bristol.dao.MetaDataMapper;
import uk.ac.bristol.dao.WarningMapper;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.Template;
import uk.ac.bristol.pojo.Warning;
import uk.ac.bristol.service.AssetService;
import uk.ac.bristol.service.ContactService;
import uk.ac.bristol.service.WarningService;
import uk.ac.bristol.util.QueryTool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WarningServiceImpl implements WarningService {

    private final MetaDataMapper metaDataMapper;
    private final WarningMapper warningMapper;
    private final ContactMapper contactMapper;
    private final AssetService assetService;
    private final ContactService contactService;

    public WarningServiceImpl(MetaDataMapper metaDataMapper, WarningMapper warningMapper, ContactMapper contactMapper, AssetService assetService, ContactService contactService) {
        this.metaDataMapper = metaDataMapper;
        this.warningMapper = warningMapper;
        this.contactMapper = contactMapper;
        this.assetService = assetService;
        this.contactService = contactService;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
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

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
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

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<Warning> getWarningById(Long id) {
        return warningMapper.selectWarningById(id);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public boolean testWarningIdExists(Long warningId) {
        return warningMapper.testWarningExists(warningId);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<Long> selectWarningIdsByAssetId(String id) {
        return warningMapper.selectWarningIdsByAssetId(id);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void storeWarningsAndSendNotifications(List<Warning> parsedWarnings) {
        List<Warning> warningsToNotify = new ArrayList<>();

        int s = 0;
        for (Warning warning : parsedWarnings) {
            s++;
            // 1. new warning: send notifications
            if (!warningMapper.testWarningExists(warning.getId())) {
                warningsToNotify.add(warning);
                warningMapper.insertWarning(warning);
                metaDataMapper.increaseTotalCountByTableName("weather_warnings", 1);
            }
            // 2. warning details updated: send notifications
            else if (warningMapper.testWarningDetailDiff(warning)) {
                warningsToNotify.add(warning);
                warningMapper.updateWarning(warning);
            }
            // 3. warning area updated: send notifications to assets not intersecting with it beforehand
            else if (warningMapper.testWarningAreaDiff(warning.getId(), warning.getAreaAsJson())) {
                List<String> oldAssetIds = assetService.selectAssetIdsByWarningId(warning.getId());
                warningMapper.updateWarning(warning);
                List<String> assetIds = assetService.selectAssetIdsByWarningId(warning.getId());
                for (String assetId : assetIds) {
                    if (!oldAssetIds.contains(assetId)) {
                        Map<String, Object> notification = contactService.formatNotification(warning.getId(), assetId);
                        contactService.sendEmail(notification);
                    }
                }
            } else {
                s--;
            }
        }

        System.out.println("Successfully inserted or updated " + s + " weather warning records at "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        for (Warning warning : warningsToNotify) {
            List<String> assetIds = assetService.selectAssetIdsByWarningId(warning.getId());
            contactService.sendAllEmails(warning, assetIds);
        }
        System.out.println("Successfully sent emails after crawling.");
    }


    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int insertWarning(Warning warning) {
        int n = warningMapper.insertWarning(warning);
        metaDataMapper.increaseTotalCountByTableName("weather_warnings", n);
        return n;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
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
            } else {
                warningMapper.insertWarning(warning);
                sum++;
            }
        }
        metaDataMapper.increaseTotalCountByTableName("weather_warnings", sum);
        return sum;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int updateWarning(Warning warning) {
        return warningMapper.updateWarning(warning);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteWarningByIDs(Long[] ids) {
        return warningMapper.deleteWarningByIDs(ids);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteWarningByIDs(List<Long> ids) {
        int n = warningMapper.deleteWarningByIDs(ids);
        metaDataMapper.increaseTotalCountByTableName("weather_warnings", -n);
        return n;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
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

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<Template> getNotificationTemplateByTypes(Template template) {
        return contactMapper.selectNotificationTemplateByTypes(template);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<Template> getNotificationTemplateById(Long id) {
        return contactMapper.selectNotificationTemplateById(id);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int insertNotificationTemplate(Template templates) {
        int n = contactMapper.insertNotificationTemplate(templates);
        metaDataMapper.increaseTotalCountByTableName("templates", n);
        return n;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int updateNotificationTemplateMessageById(Template template) {
        return contactMapper.updateNotificationTemplateMessageById(template);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int updateNotificationTemplateMessageByTypes(Template template) {
        return contactMapper.updateNotificationTemplateMessageByTypes(template);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteNotificationTemplateByIds(Long[] ids) {
        int n = contactMapper.deleteNotificationTemplateByIds(ids);
        metaDataMapper.increaseTotalCountByTableName("templates", -n);
        return n;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteNotificationTemplateByIds(List<Long> ids) {
        int n = contactMapper.deleteNotificationTemplateByIds(ids);
        metaDataMapper.increaseTotalCountByTableName("templates", -n);
        return n;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteNotificationTemplateByType(Template template) {
        int n = contactMapper.deleteNotificationTemplateByType(template);
        metaDataMapper.increaseTotalCountByTableName("templates", -n);
        return n;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public Map<String, Object> getUserInboxMessagesByUserId(String userId) {
        return contactMapper.selectUserInboxMessagesByUserId(userId);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int insertInboxMessageToUser(Map<String, Object> message) {
        return contactMapper.insertInboxMessageToUser(message);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int updateInboxMessageByUserId(Map<String, Object> message) {
        return contactMapper.updateInboxMessageByUserId(message);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteInboxMessageByFilter(Map<String, Object> filters) {
        return contactMapper.deleteInboxMessageByFilter(QueryTool.formatFilters(filters));
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteOutDatedInboxMessages() {
        return contactMapper.deleteOutDatedInboxMessages();
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteOutDatedInboxMessagesByUserId(String userId) {
        return contactMapper.deleteOutDatedInboxMessagesByUserId(userId);
    }
}
