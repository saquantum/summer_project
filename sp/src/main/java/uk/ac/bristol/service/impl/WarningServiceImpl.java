package uk.ac.bristol.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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

    public WarningServiceImpl(WarningMapper warningMapper) {
        this.warningMapper = warningMapper;
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
}
