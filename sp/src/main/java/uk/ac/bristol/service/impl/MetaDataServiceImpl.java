package uk.ac.bristol.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.dao.MetaDataMapper;
import uk.ac.bristol.pojo.TableColumnPair;
import uk.ac.bristol.service.MetaDataService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    public Map<String, Set<String>> getAllTableColumnMapsWithBlacklist(Set<String> blacklist) {
        List<TableColumnPair> pairs = metaDataMapper.selectTableColumnPairs();

        return pairs.stream()
                .filter(pair -> blacklist == null || !blacklist.contains(pair.getColumnName()))
                .collect(Collectors.groupingBy(
                        TableColumnPair::getTableName,
                        Collectors.mapping(TableColumnPair::getColumnName, Collectors.toSet())
                ));
    }
}
