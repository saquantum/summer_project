package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface MetaDataMapper {

    List<Map<String, Object>> selectAllMetaData();

    List<Map<String, Object>> selectMetaDataByTableName(@Param("tableName") String tableName);

    int increaseTotalCountByTableName(@Param("tableName") String tableName, @Param("totalCount") Integer totalCount);

    Set<String> selectAllRegisteredTableNames();

    Set<String> filterRegisteredColumnsInTables(@Param("tableNames") List<String> tableNames,
                                                @Param("columnNames") List<String> columnNames);
}
