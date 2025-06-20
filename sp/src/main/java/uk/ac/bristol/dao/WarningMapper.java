package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import uk.ac.bristol.pojo.Warning;

import java.util.List;

@Mapper
public interface WarningMapper {

    List<Warning> selectAllWarnings();

}
