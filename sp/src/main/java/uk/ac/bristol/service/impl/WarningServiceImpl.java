package uk.ac.bristol.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.ContactMapper;
import uk.ac.bristol.dao.MetaDataMapper;
import uk.ac.bristol.dao.WarningMapper;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.Template;
import uk.ac.bristol.pojo.UserWithAssets;
import uk.ac.bristol.pojo.Warning;
import uk.ac.bristol.service.AssetService;
import uk.ac.bristol.service.ContactService;
import uk.ac.bristol.service.UserService;
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
    private final UserService userService;
    private final ContactService contactService;

    public WarningServiceImpl(MetaDataMapper metaDataMapper, WarningMapper warningMapper, ContactMapper contactMapper, UserService userService, ContactService contactService) {
        this.metaDataMapper = metaDataMapper;
        this.warningMapper = warningMapper;
        this.contactMapper = contactMapper;
        this.userService = userService;
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
    public boolean storeWarningsAndSendNotifications(List<Warning> parsedWarnings) {
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
//                List<String> oldAssetIds = assetService.selectAssetIdsByWarningId(warning.getId());
//                warningMapper.updateWarning(warning);
//                List<String> assetIds = assetService.selectAssetIdsByWarningId(warning.getId());
//                for (String assetId : assetIds) {
//                    if (!oldAssetIds.contains(assetId)) {
//                        Map<String, Object> notification = contactService.formatNotification(warning.getId(), assetId);
//                        contactService.sendEmail(notification);
//                    }
//                }
            } else {
                s--;
            }
        }

        System.out.println("Successfully inserted or updated " + s + " weather warning records at "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        if (!warningsToNotify.isEmpty()) {
            for (Warning warning : warningsToNotify) {
                sendNotificationsToAllRelevantUsers(warning);
            }
            return true;
        }
        return false;
    }

    private void sendNotificationsToAllRelevantUsers(Warning warning) {
        List<UserWithAssets> list = userService.groupUsersWithOwnedAssetsByWarningId(warning.getId());
        if (list.isEmpty()) {
            System.out.println("No users has asset intersecting with warning " + warning.getId() + ", no notification has been sent.");
            return;
        }
        for (UserWithAssets uwa : list) {
            Map<String, Object> contactPreferences = uwa.getUser().getAssetHolder().getContactPreferences();

            Map<String, Object> emailNotification = contactService.formatNotification(warning, uwa, "email");
            if (contactPreferences.get("email").equals(Boolean.TRUE)) {
                contactService.sendEmailToAddress(
                        uwa.getUser().getAssetHolder().getEmail(),
                        emailNotification
                );
            }
            if (contactPreferences.get("phone").equals(Boolean.TRUE)) {
                Map<String, Object> smsNotification = contactService.formatNotification(warning, uwa, "phone");
                // send sms
            }
            if (contactPreferences.get("post").equals(Boolean.TRUE)) {
                Map<String, Object> postNotification = contactService.formatNotification(warning, uwa, "post");
                // send http post
            }

            // send inbox notification using email message
            contactMapper.insertInboxMessageToUser(Map.of(
                    "userId", emailNotification.get("toUserId"),
                    "hasRead", false,
                    "issuedDate", emailNotification.get("createdAt"),
                    "validUntil", emailNotification.get("validUntil"),
                    "title", emailNotification.get("title"),
                    "message", emailNotification.get("body")));
        }
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
}
