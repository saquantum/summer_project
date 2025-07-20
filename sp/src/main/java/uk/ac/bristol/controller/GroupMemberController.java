package uk.ac.bristol.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.pojo.GroupMember;
import uk.ac.bristol.service.GroupMemberService;

import java.util.List;

@RestController
@RequestMapping("/api/group-members")
public class GroupMemberController {

    private final GroupMemberService groupMemberService;

    @Autowired
    public GroupMemberController(GroupMemberService groupMemberService) {
        this.groupMemberService = groupMemberService;
    }

    @PostMapping
    public void addGroupMember(@RequestBody GroupMember groupMember) {
        groupMemberService.addGroupMember(groupMember);
    }

    @DeleteMapping("/{id}")
    public void deleteGroupMember(@PathVariable Integer id) {
        groupMemberService.removeGroupMemberById(id);
    }

    @PutMapping
    public void updateGroupMember(@RequestBody GroupMember groupMember) {
        groupMemberService.updateGroupMember(groupMember);
    }

    @GetMapping("/group/{groupId}")
    public List<GroupMember> getMembersByGroupId(@PathVariable Integer groupId) {
        return groupMemberService.getMembersByGroupId(groupId);
    }
}
