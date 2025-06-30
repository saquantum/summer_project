package uk.ac.bristol.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.AssetHolderMapper;
import uk.ac.bristol.dao.UserMapper;
import uk.ac.bristol.dao.WarningMapper;
import uk.ac.bristol.pojo.Warning;
import uk.ac.bristol.service.WarningService;
import uk.ac.bristol.util.QueryTool;

import java.util.List;
import java.util.Map;

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
}
