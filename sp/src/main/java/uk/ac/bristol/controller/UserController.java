package uk.ac.bristol.controller;

import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.service.PermissionConfigService;
import uk.ac.bristol.service.UserService;
import uk.ac.bristol.util.QueryTool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    private final UserService userService;
    private final PermissionConfigService permissionConfigService;

    public UserController(UserService userService, PermissionConfigService permissionConfigService) {
        this.userService = userService;
        this.permissionConfigService = permissionConfigService;
    }

    /* --------- for users and admin proxy --------- */

    @GetMapping("/uid/{uid}")
    public ResponseBody getMyProfileByUserId(HttpServletResponse response,
                                             HttpServletRequest request,
                                             @PathVariable String uid) {
        if (!QueryTool.userIdentityVerification(response, request, uid, null)) {
            throw new SpExceptions.GetMethodException("User identification failed");
        }
        User user = userService.getUserByUserId(uid);
        user.setPassword(null);
        user.setPermissionConfig(QueryTool.getUserPermissions(uid, null));
        return new ResponseBody(Code.SELECT_OK, user);
    }

    @GetMapping("/aid/{aid}")
    public ResponseBody getMyProfileByAssetHolderId(HttpServletResponse response,
                                                    HttpServletRequest request,
                                                    @PathVariable String aid) {
        if (!QueryTool.userIdentityVerification(response, request, null, aid)) {
            throw new SpExceptions.GetMethodException("User identification failed");
        }
        User user = userService.getUserByAssetHolderId(aid, null, null, null);
        user.setPassword(null);
        user.setPermissionConfig(QueryTool.getUserPermissions(null, aid));
        return new ResponseBody(Code.SELECT_OK, user);
    }

    // NOTICE: No Post Mapping. A common user cannot insert new users, unless they access login controller to register

    @PutMapping("/uid/{uid}")
    public ResponseBody updateMyProfileWithUserId(HttpServletResponse response,
                                                  HttpServletRequest request,
                                                  @PathVariable String uid,
                                                  @RequestBody User user) {
        if (!QueryTool.userIdentityVerification(response, request, uid, null)) {
            throw new SpExceptions.PutMethodException("User identification failed");
        }
        if(!QueryTool.getUserPermissions(uid, null).getCanUpdateProfile()){
            throw new SpExceptions.PutMethodException("The user is not allowed to update profile");
        }
        userService.updateUser(user);
        return new ResponseBody(Code.UPDATE_OK, null);
    }

    @PutMapping("/aid/{aid}")
    public ResponseBody updateMyProfileWithAssetHolderId(HttpServletResponse response,
                                                         HttpServletRequest request,
                                                         @PathVariable String aid,
                                                         @RequestBody User user) {
        if (!QueryTool.userIdentityVerification(response, request, null, aid)) {
            throw new SpExceptions.PutMethodException("User identification failed");
        }
        if(!QueryTool.getUserPermissions(null, aid).getCanUpdateProfile()){
            throw new SpExceptions.PutMethodException("The user is not allowed to update profile");
        }
        userService.updateUser(user);
        return new ResponseBody(Code.UPDATE_OK, null);
    }

    @DeleteMapping("/uid/{uid}")
    public ResponseBody deleteMyProfileWithUserId(HttpServletResponse response,
                                                  HttpServletRequest request,
                                                  @PathVariable String uid) {
        if (!QueryTool.userIdentityVerification(response, request, uid, null)) {
            throw new SpExceptions.DeleteMethodException("User identification failed");
        }
        userService.deleteUserByUserIds(new String[]{uid});
        return new ResponseBody(Code.DELETE_OK, null);
    }

    @DeleteMapping("/aid/{aid}")
    public ResponseBody deleteMyProfileWithAssetHolderId(HttpServletResponse response,
                                                         HttpServletRequest request,
                                                         @PathVariable String aid) {
        if (!QueryTool.userIdentityVerification(response, request, null, aid)) {
            throw new SpExceptions.DeleteMethodException("User identification failed");
        }
        userService.deleteUserByAssetHolderIds(new String[]{aid});
        return new ResponseBody(Code.DELETE_OK, null);
    }

    @GetMapping("/uid/{uid}/permission")
    public ResponseBody getMyPermission(HttpServletResponse response,
                                        HttpServletRequest request,
                                        @PathVariable String uid) {
        if (!QueryTool.userIdentityVerification(response, request, uid, null)) {
            throw new SpExceptions.DeleteMethodException("User identification failed");
        }
        return new ResponseBody(Code.SELECT_OK, QueryTool.getUserPermissions(uid, null));
    }
}
