package uk.ac.bristol.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MetaDataService {

    List<Map<String, Object>> getAllMetaData();

    List<Map<String, Object>> getMetaDataByTableName(String tableName);

    Set<String> getAllRegisteredTableNames();

    Set<String> getAllRegisteredColumnNamesWithBlacklist(List<String> blacklist);

    Map<String, Set<String>> getAllTableColumnMapsWithBlacklist(Set<String> blacklist);
}
