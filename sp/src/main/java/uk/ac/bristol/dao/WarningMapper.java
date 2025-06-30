package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import uk.ac.bristol.pojo.Warning;

import java.util.List;
import java.util.Map;

@Mapper
public interface WarningMapper {

    List<Warning> selectAllWarnings(@Param("orderList") List<Map<String, String>> orderList,
                                    @Param("limit") Integer limit,
                                    @Param("offset") Integer offset);

    List<Warning> selectAllWarningsIncludingOutdated(@Param("orderList") List<Map<String, String>> orderList,
                                                     @Param("limit") Integer limit,
                                                     @Param("offset") Integer offset);

    List<Warning> selectWarningById(@Param("id") Long id);

    int insertWarning(Warning warning);

    int updateWarning(Warning warning);

    int deleteWarningByIDs(@Param("ids") Long[] ids);

    int deleteWarningByIDs(@Param("ids") List<Long> ids);

    List<Map<String, Object>> selectAllNotificationTemplates();

    int insertNotificationTemplate(@Param("message") String message);

    int updateNotificationTemplate(Map<String, String> template);

    int deleteNotificationTemplateByIds(@Param("ids") Integer[] ids);

    int deleteNotificationTemplateByIds(@Param("ids") List<Integer> ids);
}
