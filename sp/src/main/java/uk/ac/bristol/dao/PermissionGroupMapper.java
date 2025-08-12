package uk.ac.bristol.dao;

import org.apache.ibatis.annotations.Mapper;
import uk.ac.bristol.pojo.PermissionGroup;
import java.util.List;

@Mapper
public interface PermissionGroupMapper {
    List<PermissionGroup> selectAllGroups();
    PermissionGroup selectGroupById(Long groupId);
    void insertGroup(PermissionGroup group);
    void updateGroup(PermissionGroup group);
    void deleteGroup(Long groupId);

    PermissionGroup selectGroupByName(String groupName);
}