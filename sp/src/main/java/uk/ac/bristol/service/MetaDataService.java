package uk.ac.bristol.service;

import uk.ac.bristol.pojo.ColumnTriple;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MetaDataService {

    List<Map<String, Object>> getAllMetaData();

    List<Map<String, Object>> getMetaDataByTableName(String tableName);

    Set<String> getAllRegisteredTableNames();

    Set<String> getAllRegisteredColumnNamesWithBlacklist(List<String> blacklist);

    List<ColumnTriple> getAllTableColumnMapsWithBlacklist(Set<String> blacklist);
}
