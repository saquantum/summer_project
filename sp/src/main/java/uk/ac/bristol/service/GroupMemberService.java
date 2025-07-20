package uk.ac.bristol.service;

import uk.ac.bristol.pojo.GroupMember;

import java.util.List;

public interface GroupMemberService {
    void addGroupMember(GroupMember groupMember);

    void removeGroupMemberById(Integer id);

    void updateGroupMember(GroupMember groupMember);

    List<GroupMember> getMembersByGroupId(Integer groupId);
}