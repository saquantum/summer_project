package uk.ac.bristol.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.MetaDataMapper;
import uk.ac.bristol.service.MetaDataService;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
@Service
public class MetaDataServiceImpl implements MetaDataService {

    private final MetaDataMapper metaDataMapper;

    public MetaDataServiceImpl(MetaDataMapper metaDataMapper) {
        this.metaDataMapper = metaDataMapper;
    }

    @Override
    public List<Map<String, Object>> getAllMetaData() {
        return metaDataMapper.selectAllMetaData();
    }

    @Override
    public List<Map<String, Object>> getMetaDataByTableName(String tableName) {
        return metaDataMapper.selectMetaDataByTableName(tableName);
    }

    @Override
    public Set<String> filterRegisteredColumnsInTables(List<String> tableNames, List<String> columnNames) {
        return metaDataMapper.filterRegisteredColumnsInTables(tableNames, columnNames);
    }
}
