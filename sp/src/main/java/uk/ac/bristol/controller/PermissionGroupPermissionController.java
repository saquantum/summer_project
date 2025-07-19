package uk.ac.bristol.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.pojo.PermissionGroupPermission;
import uk.ac.bristol.service.PermissionGroupPermissionService;

@RestController
@RequestMapping("/api/group-permissions")
public class PermissionGroupPermissionController {

    private final PermissionGroupPermissionService permissionGroupPermissionService;

    @Autowired
    public PermissionGroupPermissionController(PermissionGroupPermissionService permissionGroupPermissionService) {
        this.permissionGroupPermissionService = permissionGroupPermissionService;
    }

    @GetMapping("/{groupId}")
    public PermissionGroupPermission getPermissionsByGroupId(@PathVariable Long groupId) {
        return permissionGroupPermissionService.getPermissionsByGroupId(groupId);
    }

    @PostMapping
    public void createPermissions(@RequestBody PermissionGroupPermission permissions) {
        permissionGroupPermissionService.addPermissions(permissions);
    }

    @PutMapping("/{groupId}")
    public void updatePermissions(@PathVariable Long groupId, @RequestBody PermissionGroupPermission permissions) {
        permissions.setGroupId(groupId);
        permissionGroupPermissionService.updatePermissions(permissions);
    }

    @DeleteMapping("/{groupId}")
    public void deletePermissions(@PathVariable Long groupId) {
        permissionGroupPermissionService.deletePermissions(groupId);
    }
}