package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import uk.ac.bristol.pojo.ColumnTriple;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface MetaDataMapper {

    List<Map<String, Object>> selectAllMetaData();

    List<Map<String, Object>> selectMetaDataByTableName(@Param("tableName") String tableName);

    int increaseTotalCountByTableName(@Param("tableName") String tableName, @Param("totalCount") Integer totalCount);

    Set<String> selectAllRegisteredTableNames();

    Set<String> selectAllRegisteredColumnNames();

    List<ColumnTriple> selectTableColumnPairs();
}
