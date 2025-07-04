package uk.ac.bristol.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.Settings;
import uk.ac.bristol.service.MetaDataService;

import java.util.List;
import java.util.Map;

@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
@Service
public class MetaDataServiceImpl implements MetaDataService {

    private final Settings settings;

    public MetaDataServiceImpl(Settings settings) {
        this.settings = settings;
    }

    @Override
    public List<Map<String, Object>> getAllMetaData() {
        return settings.selectAllMetaData();
    }

    @Override
    public List<Map<String, Object>> getMetaDataByTableName(String tableName) {
        return settings.selectMetaDataByTableName(tableName);
    }
}
