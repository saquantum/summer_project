package uk.ac.bristol.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.AssetHolderMapper;
import uk.ac.bristol.dao.UserMapper;
import uk.ac.bristol.dao.WarningMapper;
import uk.ac.bristol.pojo.AssetType;
import uk.ac.bristol.pojo.UserWithAssetHolder;
import uk.ac.bristol.pojo.Warning;
import uk.ac.bristol.service.WarningService;
import uk.ac.bristol.util.QueryTool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
@Service
public class WarningServiceImpl implements WarningService {

    private final WarningMapper warningMapper;
    private final UserMapper userMapper;
    private final AssetHolderMapper assetHolderMapper;

    public WarningServiceImpl(WarningMapper warningMapper, UserMapper userMapper, AssetHolderMapper assetHolderMapper) {
        this.warningMapper = warningMapper;
        this.userMapper = userMapper;
        this.assetHolderMapper = assetHolderMapper;
    }

    @Override
    public List<Warning> getAllWarnings(List<Map<String, String>> orderList,
                                        Integer limit,
                                        Integer offset) {
        return warningMapper.selectAllWarnings(QueryTool.filterOrderList(orderList, "warning"), limit, offset);
    }

    @Override
    public List<Warning> getAllWarningsIncludingOutdated(List<Map<String, String>> orderList,
                                                         Integer limit,
                                                         Integer offset) {
        return warningMapper.selectAllWarningsIncludingOutdated(QueryTool.filterOrderList(orderList, "warning"), limit, offset);
    }

    @Override
    public List<Warning> getWarningById(Long id) {
        return warningMapper.selectWarningById(id);
    }

    @Override
    public int insertWarning(Warning warning) {
        return warningMapper.insertWarning(warning);
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
        return warningMapper.deleteWarningByIDs(ids);
    }

    @Override
    public List<Map<String, Object>> getAllNotificationTemplates() {
        return warningMapper.selectAllNotificationTemplates();
    }

    @Override
    public int insertNotificationTemplate(String message) {
        return warningMapper.insertNotificationTemplate(message);
    }

    @Override
    public int updateNotificationTemplate(Map<String, String> template) {
        return warningMapper.updateNotificationTemplate(template);
    }

    @Override
    public int deleteNotificationTemplateByIds(Integer[] ids) {
        return warningMapper.deleteNotificationTemplateByIds(ids);
    }

    @Override
    public int deleteNotificationTemplateByIds(List<Integer> ids) {
        return warningMapper.deleteNotificationTemplateByIds(ids);
    }

    @Override
    public List<Map<String, Object>> sendNotifications(Warning warning, AssetType type, String message) {
        List<Map<String, Object>> notifications = new ArrayList<>();

        String id = UUID.randomUUID().toString();
        String typeId = type.getId();
        String warningType = warning.getWeatherType();
        String warningSeverity = warning.getWarningLevel();
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> notification = new HashMap<>(Map.of(
                "id", id,
                "assetTypeId", typeId,
                "weatherWarningType", warningType,
                "severity", warningSeverity, "message", message,
                "createdAt", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "channel", ""));
        List<UserWithAssetHolder> list = userMapper.selectAllUnauthorisedUsersWithAssetHolder(null, null, null);
        for (UserWithAssetHolder uwa : list) {
            List<Map<String, Object>> contactPreferences = assetHolderMapper.selectContactPreferencesByAssetHolderId(uwa.getAssetHolder().getId());
            if (contactPreferences.size() != 1) {
                throw new RuntimeException("Get " + contactPreferences.size() + " contact preferences for asset holder " + uwa.getAssetHolder().getId());
            }
            System.out.println(contactPreferences.get(0));
            if ((Boolean) contactPreferences.get(0).get("email")) {
                // send email here
                notification.put("channel", "email");
                notifications.add(notification);
            }
            if ((Boolean) contactPreferences.get(0).get("phone")) {
                // send SMS here
                notification.put("channel", "sms");
                notifications.add(notification);
            }
            if ((Boolean) contactPreferences.get(0).get("post")) {
                // send post here
                notification.put("channel", "post");
                notifications.add(notification);
            }
        }
        return notifications;
    }
}
