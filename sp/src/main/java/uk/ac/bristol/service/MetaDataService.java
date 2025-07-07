package uk.ac.bristol.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MetaDataService {

    List<Map<String, Object>> getAllMetaData();

    List<Map<String, Object>> getMetaDataByTableName(String tableName);

    Set<String> filterRegisteredColumnsInTables(List<String> tableNames, List<String> columnNames);
}
