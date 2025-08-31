package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import uk.ac.bristol.pojo.AccessControlGroup;
import uk.ac.bristol.pojo.FilterItemDTO;

import java.util.List;
import java.util.Map;

@Mapper
public interface AccessControlMapper {

    List<AccessControlGroup> selectAccessControlGroups(@Param("filterList") List<FilterItemDTO> filterList,
                                                       @Param("orderList") List<Map<String, String>> orderList,
                                                       @Param("limit") Integer limit,
                                                       @Param("offset") Integer offset);

    List<Map<String, Object>> selectAccessControlGroupAnchor(@Param("rowId") Long rowId);

    List<AccessControlGroup> selectAccessControlGroupsByUserId(@Param("userId") String userId);

    int insertAccessControlGroup(AccessControlGroup permissionConfig);

    int updateAccessControlGroup(AccessControlGroup permissionConfig);

    int deleteAccessControlGroupByRowIds(@Param("ids") Long[] rowIds);

    int deleteAccessControlGroupByRowIds(@Param("ids") List<Long> rowIds);

    Map<String, Integer> upsertUserAccessControlGroupMapping(@Param("groupName") String groupName, @Param("filterList") List<FilterItemDTO> filterList);
}