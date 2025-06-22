package uk.ac.bristol.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.WarningMapper;
import uk.ac.bristol.pojo.Warning;
import uk.ac.bristol.service.WarningService;

import java.util.List;

@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
@Service
public class WarningServiceImpl implements WarningService {

    private final WarningMapper warningMapper;

    public WarningServiceImpl(WarningMapper warningMapper) {
        this.warningMapper = warningMapper;
    }

    @Override
    public List<Warning> getAllWarnings() {
        return warningMapper.selectAllWarnings();
    }

    @Override
    public List<Warning> getAllWarningsIncludingOutdated() {
        return warningMapper.selectAllWarningsIncludingOutdated();
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
}
