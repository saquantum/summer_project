package uk.ac.bristol.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.pojo.ApiResponse;
import uk.ac.bristol.pojo.PermissionGroupPermission;
import uk.ac.bristol.service.PermissionGroupPermissionService;
import uk.ac.bristol.pojo.GroupMember;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PermissionGroupPermissionController {

    private final PermissionGroupPermissionService permissionGroupPermissionService;

    @Autowired
    public PermissionGroupPermissionController(PermissionGroupPermissionService permissionGroupPermissionService) {
        this.permissionGroupPermissionService = permissionGroupPermissionService;
    }

    @GetMapping("/group-permissions/{groupId}")
    public List<PermissionGroupPermission> getPermissionsByGroupId(@PathVariable Long groupId) {
        return permissionGroupPermissionService.getPermissionsByGroupId(groupId);
    }

    @PostMapping("/post-group-permissions")
    public void createPermissions(@RequestBody PermissionGroupPermission permissions) {
        permissionGroupPermissionService.addPermissions(permissions);
    }

    @PutMapping("/group-permissions/{groupId}")
    public ApiResponse<Void> updatePermissions(@PathVariable Long groupId, @RequestBody PermissionGroupPermission permissions) {
        permissions.setGroupId(groupId);
        permissionGroupPermissionService.updatePermissions(permissions);
        return new ApiResponse<>(200, "Permissions updated successfully", null);
    }

    @DeleteMapping("/group-permissions/{groupId}")
    public void deletePermissions(@PathVariable Long groupId) {
        permissionGroupPermissionService.deletePermissions(groupId);
    }
}