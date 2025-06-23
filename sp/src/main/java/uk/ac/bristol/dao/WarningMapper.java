package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import uk.ac.bristol.pojo.Warning;

import java.util.List;

@Mapper
public interface WarningMapper {

    List<Warning> selectAllWarnings();

    List<Warning> selectAllWarningsIncludingOutdated();

    List<Warning> selectWarningById(@Param("id") Long id);

    int insertWarning(Warning warning);

    int updateWarning(Warning warning);

    int deleteWarningByIDs(@Param("ids") Long[] ids);

    int deleteWarningByIDs(@Param("ids") List<Long> ids);
}
