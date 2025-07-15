package uk.ac.bristol.controller;

import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.advice.UserAID;
import uk.ac.bristol.advice.UserUID;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.service.UserService;
import uk.ac.bristol.util.QueryTool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    private final UserService userService;

    /**
     * NOTICE: For methods of names starting with 'user' and ending with 'UID' or 'AID',
     * an aspect of checking user identity is implemented. See advice.UserIdentificationAdvice file.
     * Also, DO NOT delete the servlet parameters passed into methods which might be marked as 'unused'
     * by IDEA. These parameters are useful in the above AOP aspects.
     *
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /* --------- for users and admin proxy --------- */

    @GetMapping("/uid/{uid}")
    public ResponseBody userGetMyProfileByUID(HttpServletResponse response,
                                              HttpServletRequest request,
                                              @UserUID @PathVariable String uid) {
        User user = userService.getUserByUserId(uid);
        user.setPassword(null);
        user.setPermissionConfig(QueryTool.getUserPermissions(uid, null));
        return new ResponseBody(Code.SELECT_OK, user);
    }

    @GetMapping("/aid/{aid}")
    public ResponseBody userGetMyProfileByAID(HttpServletResponse response,
                                              HttpServletRequest request,
                                              @UserAID @PathVariable String aid) {
        User user = userService.getUserByAssetHolderId(aid);
        user.setPassword(null);
        user.setPermissionConfig(QueryTool.getUserPermissions(null, aid));
        return new ResponseBody(Code.SELECT_OK, user);
    }

    // NOTICE: No Post Mapping. A common user cannot insert new users, unless they access login controller to register

    @PutMapping("/uid/{uid}")
    public ResponseBody userUpdateMyProfileWithUID(HttpServletResponse response,
                                                   HttpServletRequest request,
                                                   @UserUID @PathVariable String uid,
                                                   @RequestBody User user) {
        if (!QueryTool.getUserPermissions(uid, null).getCanUpdateProfile()) {
            throw new SpExceptions.ForbiddenException("The user is not allowed to update profile");
        }
        userService.updateUser(user);
        return new ResponseBody(Code.UPDATE_OK, null);
    }

    @PutMapping("/aid/{aid}")
    public ResponseBody userUpdateMyProfileWithAID(HttpServletResponse response,
                                                   HttpServletRequest request,
                                                   @UserAID @PathVariable String aid,
                                                   @RequestBody User user) {
        if (!QueryTool.getUserPermissions(null, aid).getCanUpdateProfile()) {
            throw new SpExceptions.ForbiddenException("The user is not allowed to update profile");
        }
        userService.updateUser(user);
        return new ResponseBody(Code.UPDATE_OK, null);
    }

    @DeleteMapping("/uid/{uid}")
    public ResponseBody userDeleteMyProfileWithUID(HttpServletResponse response,
                                                   HttpServletRequest request,
                                                   @UserUID @PathVariable String uid) {
        userService.deleteUserByUserIds(new String[]{uid});
        return new ResponseBody(Code.DELETE_OK, null);
    }

    @DeleteMapping("/aid/{aid}")
    public ResponseBody userDeleteMyProfileWithAID(HttpServletResponse response,
                                                   HttpServletRequest request,
                                                   @UserAID @PathVariable String aid) {
        userService.deleteUserByAssetHolderIds(new String[]{aid});
        return new ResponseBody(Code.DELETE_OK, null);
    }

    @GetMapping("/uid/{uid}/permission")
    public ResponseBody userGetMyPermissionByUID(HttpServletResponse response,
                                                 HttpServletRequest request,
                                                 @UserUID @PathVariable String uid) {
        return new ResponseBody(Code.SELECT_OK, QueryTool.getUserPermissions(uid, null));
    }
}
