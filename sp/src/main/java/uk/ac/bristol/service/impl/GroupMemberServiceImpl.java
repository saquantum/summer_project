package uk.ac.bristol.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import uk.ac.bristol.dao.GroupMemberMapper;
import uk.ac.bristol.pojo.GroupMember;
import uk.ac.bristol.service.GroupMemberService;

import java.util.List;

@Service
public class GroupMemberServiceImpl implements GroupMemberService {

    private final GroupMemberMapper groupMemberMapper;

    @Autowired
    public GroupMemberServiceImpl(GroupMemberMapper groupMemberMapper) {
        this.groupMemberMapper = groupMemberMapper;
    }

    @Override
    public void addGroupMember(GroupMember groupMember) {
        groupMemberMapper.insertGroupMember(groupMember);
    }

    @Override
    public void removeGroupMemberById(Integer id) {
        groupMemberMapper.deleteGroupMemberById(id);
    }

    @Override
    public void updateGroupMember(GroupMember groupMember) {
        groupMemberMapper.updateGroupMember(groupMember);
    }

    @Override
    public List<GroupMember> getMembersByGroupId(Integer groupId) {
        return groupMemberMapper.getMembersByGroupId(groupId);
    }
}
