package uk.ac.bristol.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.pojo.ApiResponse;
import uk.ac.bristol.pojo.GroupMember;
import uk.ac.bristol.service.GroupMemberService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GroupMemberController {

    private final GroupMemberService groupMemberService;

    @Autowired
    public GroupMemberController(GroupMemberService groupMemberService) {
        this.groupMemberService = groupMemberService;
    }

    @PostMapping("/group-members")
    public ApiResponse<GroupMember> addGroupMember(@RequestBody GroupMember member) {
        GroupMember createdMember = groupMemberService.addGroupMember(member);
        return new ApiResponse<>(200, "Member added successfully", createdMember);
    }

    @DeleteMapping("/group-members/{id}")
    public ApiResponse<Void> deleteGroupMember(@PathVariable Integer id) {
        boolean success = groupMemberService.removeGroupMemberById(id);
        if (success) {
            return new ApiResponse<>(200, "Member deleted successfully", null);
        } else {
            return new ApiResponse<>(404, "Member not found", null);
        }
    }

    @PutMapping("/group-members")
    public void updateGroupMember(@RequestBody GroupMember groupMember) {
        groupMemberService.updateGroupMember(groupMember);
    }

    @GetMapping("/group/{groupId}")
    public List<GroupMember> getMembersByGroupId(@PathVariable Integer groupId) {
        return groupMemberService.getMembersByGroupId(groupId);
    }
}
