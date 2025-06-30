package uk.ac.bristol.controller;

import org.springframework.web.bind.annotation.*;
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

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /* --------- for users and admin proxy --------- */

    @GetMapping("/uid/{id}")
    public ResponseBody getMyProfileByUserId(HttpServletResponse response,
                                             HttpServletRequest request,
                                             @PathVariable String id) {
        if (!QueryTool.userIdentityVerification(response, request, id, null)) {
            throw new SpExceptions.GetMethodException("User identification failed");
        }
        User user = userService.getUserByUserId(id, null, null, null);
        user.setPassword(null);
        return new ResponseBody(Code.SELECT_OK, user);
    }

    @GetMapping("/aid/{id}")
    public ResponseBody getMyProfileByAssetHolderId(HttpServletResponse response,
                                                    HttpServletRequest request,
                                                    @PathVariable String id) {
        if (!QueryTool.userIdentityVerification(response, request, null, id)) {
            throw new SpExceptions.GetMethodException("User identification failed");
        }
        User user = userService.getUserByAssetHolderId(id, null, null, null);
        user.setPassword(null);
        return new ResponseBody(Code.SELECT_OK, user);
    }

    // NOTICE: No Post Mapping. A common user cannot insert new users, unless they access login controller to register

    @PutMapping("/uid/{id}")
    public ResponseBody updateMyProfileWithUserId(HttpServletResponse response,
                                                  HttpServletRequest request,
                                                  @PathVariable String id,
                                                  @RequestBody User user) {
        if (!QueryTool.userIdentityVerification(response, request, id, null)) {
            throw new SpExceptions.PostMethodException("User identification failed");
        }
        userService.updateUser(user);
        return new ResponseBody(Code.UPDATE_OK, null);
    }

    @PutMapping("/aid/{id}")
    public ResponseBody updateMyProfileWithAssetHolderId(HttpServletResponse response,
                                                         HttpServletRequest request,
                                                         @PathVariable String id,
                                                         @RequestBody User user) {
        if (!QueryTool.userIdentityVerification(response, request, null, id)) {
            throw new SpExceptions.PostMethodException("User identification failed");
        }
        userService.updateUser(user);
        return new ResponseBody(Code.UPDATE_OK, null);
    }

    @DeleteMapping("/uid/{id}")
    public ResponseBody deleteMyProfileWithUserId(HttpServletResponse response,
                                                  HttpServletRequest request,
                                                  @PathVariable String id) {
        if (!QueryTool.userIdentityVerification(response, request, id, null)) {
            throw new SpExceptions.DeleteMethodException("User identification failed");
        }
        userService.deleteUserByUserIds(new String[]{id});
        return new ResponseBody(Code.DELETE_OK, null);
    }

    @DeleteMapping("/aid/{id}")
    public ResponseBody deleteMyProfileWithAssetHolderId(HttpServletResponse response,
                                                         HttpServletRequest request,
                                                         @PathVariable String id) {
        if (!QueryTool.userIdentityVerification(response, request, null, id)) {
            throw new SpExceptions.DeleteMethodException("User identification failed");
        }
        userService.deleteUserByAssetHolderIds(new String[]{id});
        return new ResponseBody(Code.DELETE_OK, null);
    }


}
