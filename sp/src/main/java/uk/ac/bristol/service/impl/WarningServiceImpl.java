package uk.ac.bristol.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.AssetHolderMapper;
import uk.ac.bristol.dao.Settings;
import uk.ac.bristol.dao.UserMapper;
import uk.ac.bristol.dao.WarningMapper;
import uk.ac.bristol.pojo.Template;
import uk.ac.bristol.pojo.Warning;
import uk.ac.bristol.service.WarningService;
import uk.ac.bristol.util.QueryTool;

import java.util.List;
import java.util.Map;

@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
@Service
public class WarningServiceImpl implements WarningService {

    private final Settings settings;
    private final WarningMapper warningMapper;
    private final UserMapper userMapper;
    private final AssetHolderMapper assetHolderMapper;

    public WarningServiceImpl(Settings settings, WarningMapper warningMapper, UserMapper userMapper, AssetHolderMapper assetHolderMapper) {
        this.settings = settings;
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
        int n = warningMapper.insertWarning(warning);
        settings.increaseTotalCountByTableName("weather_warnings", n);
        return n;
    }

    @Override
    public int insertWarningsList(List<Warning> warnings) {
        if (warnings.isEmpty()) return 0;
        int sum = 0;
        for (Warning warning : warnings) {
            List<Warning> search = warningMapper.selectWarningById(warning.getId());
            if (search.size() > 1) {
                throw new RuntimeException("Found multiple weather warning data stored in database for id " + warning.getId());
            } else if (search.size() == 1) {
                warningMapper.updateWarning(warning);
                sum++;
            } else {
                warningMapper.insertWarning(warning);
                sum++;
            }
        }
        settings.increaseTotalCountByTableName("weather_warnings", sum);
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
        settings.increaseTotalCountByTableName("weather_warnings", -n);
        return n;
    }

    @Override
    public List<Template> getAllNotificationTemplates(List<Map<String, String>> orderList,
                                                      Integer limit,
                                                      Integer offset) {
        return warningMapper.selectAllNotificationTemplates(QueryTool.filterOrderList(orderList, "template"), limit, offset);
    }

    @Override
    public List<Template> getNotificationTemplateByTypes(Template template) {
        return warningMapper.selectNotificationTemplateByTypes(template);
    }

    @Override
    public List<Template> getNotificationTemplateById(Long id) {
        return warningMapper.selectNotificationTemplateById(id);
    }

    @Override
    public int insertNotificationTemplate(Template templates) {
        int n = warningMapper.insertNotificationTemplate(templates);
        settings.increaseTotalCountByTableName("templates", n);
        return n;
    }

    @Override
    public int updateNotificationTemplateMessageById(Template template) {
        return warningMapper.updateNotificationTemplateMessageById(template);
    }

    @Override
    public int updateNotificationTemplateMessageByTypes(Template template) {
        return warningMapper.updateNotificationTemplateMessageByTypes(template);
    }

    @Override
    public int deleteNotificationTemplateByIds(Long[] ids) {
        int n = warningMapper.deleteNotificationTemplateByIds(ids);
        settings.increaseTotalCountByTableName("templates", -n);
        return n;
    }

    @Override
    public int deleteNotificationTemplateByIds(List<Long> ids) {
        int n = warningMapper.deleteNotificationTemplateByIds(ids);
        settings.increaseTotalCountByTableName("templates", -n);
        return n;
    }

    @Override
    public int deleteNotificationTemplateByType(Template template) {
        int n = warningMapper.deleteNotificationTemplateByType(template);
        settings.increaseTotalCountByTableName("templates", -n);
        return n;
    }
}
