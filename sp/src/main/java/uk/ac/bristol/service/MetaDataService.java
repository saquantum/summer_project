package uk.ac.bristol.service;

import java.util.List;
import java.util.Map;

public interface MetaDataService {

    List<Map<String, Object>> getAllMetaData();

    List<Map<String, Object>> getMetaDataByTableName(String tableName);
}
