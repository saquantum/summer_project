package uk.ac.bristol.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.MetaDataMapper;
import uk.ac.bristol.pojo.ColumnTriple;
import uk.ac.bristol.service.MetaDataService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MetaDataServiceImpl implements MetaDataService {

    private final MetaDataMapper metaDataMapper;

    public MetaDataServiceImpl(MetaDataMapper metaDataMapper) {
        this.metaDataMapper = metaDataMapper;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<Map<String, Object>> getAllMetaData() {
        return metaDataMapper.selectAllMetaData();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<Map<String, Object>> getMetaDataByTableName(String tableName) {
        return metaDataMapper.selectMetaDataByTableName(tableName);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public Set<String> getAllRegisteredTableNames() {
        return metaDataMapper.selectAllRegisteredTableNames();
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public Set<String> getAllRegisteredColumnNamesWithBlacklist(List<String> blacklist) {
        Set<String> columns = metaDataMapper.selectAllRegisteredColumnNames();
        if (blacklist == null || blacklist.isEmpty()) {
            return columns;
        }
        blacklist.forEach(columns::remove);
        return columns;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<ColumnTriple> getAllTableColumnMapsWithBlacklist(Set<String> blacklist) {
        List<ColumnTriple> pairs = metaDataMapper.selectTableColumnPairs();
        List<ColumnTriple> result = new ArrayList<>();
        for (ColumnTriple pair : pairs) {
            if (blacklist == null || !blacklist.contains(pair.getColumnName())) {
                result.add(pair);
            }
        }
        return result;
    }
}
