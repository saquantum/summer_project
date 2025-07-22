package uk.ac.bristol.dao;

import uk.ac.bristol.pojo.GroupMember;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface GroupMemberMapper {
    void insertGroupMember(GroupMember groupMember);
    void deleteGroupMemberById(Integer id);
    void updateGroupMember(GroupMember groupMember);
    List<GroupMember> getMembersByGroupId(Integer groupId);
}