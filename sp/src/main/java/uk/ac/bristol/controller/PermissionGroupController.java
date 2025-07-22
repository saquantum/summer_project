package uk.ac.bristol.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.pojo.PermissionGroup;
import uk.ac.bristol.service.PermissionGroupService;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class PermissionGroupController {

    private final PermissionGroupService permissionGroupService;

    @Autowired
    public PermissionGroupController(PermissionGroupService permissionGroupService) {
        this.permissionGroupService = permissionGroupService;
    }

    @GetMapping
    public List<PermissionGroup> getAllGroups() {
        return permissionGroupService.getAllGroups();
    }

    @GetMapping("/{groupId}")
    public PermissionGroup getGroupById(@PathVariable Long groupId) {
        return permissionGroupService.getGroupById(groupId);
    }

    @PostMapping
    public void createGroup(@RequestBody PermissionGroup group) {
        permissionGroupService.addGroup(group);
    }

    @PutMapping("/{groupId}")
    public void updateGroup(@PathVariable Long groupId, @RequestBody PermissionGroup group) {
        group.setGroupId(groupId);
        permissionGroupService.updateGroup(group);
    }

    @DeleteMapping("/{groupId}")
    public void deleteGroup(@PathVariable Long groupId) {
        permissionGroupService.deleteGroup(groupId);
    }
}
