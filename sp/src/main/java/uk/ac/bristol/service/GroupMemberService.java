package uk.ac.bristol.service;

import uk.ac.bristol.pojo.GroupMember;
import java.util.List;

public interface GroupMemberService {
    GroupMember addGroupMember(GroupMember groupMember);

    boolean removeGroupMemberById(Integer id);

    void updateGroupMember(GroupMember groupMember);

    List<GroupMember> getMembersByGroupId(Integer groupId);
}
