package uk.ac.bristol.controller;

import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.advice.UserAID;
import uk.ac.bristol.advice.UserUID;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.FilterDTO;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.service.UserService;
import uk.ac.bristol.util.QueryTool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
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

    /* --------- *************************** --------- */
    /* --------- interfaces for common users --------- */
    /* --------- *************************** --------- */

    @GetMapping("/user/uid/{uid}")
    public ResponseBody userGetMyProfileByUID(HttpServletResponse response,
                                              HttpServletRequest request,
                                              @UserUID @PathVariable String uid) {
        User user = userService.getUserByUserId(uid);
        user.setPassword(null);
        user.setPermissionConfig(QueryTool.getUserPermissions(uid, null));
        return new ResponseBody(Code.SELECT_OK, user);
    }

    @GetMapping("/user/aid/{aid}")
    public ResponseBody userGetMyProfileByAID(HttpServletResponse response,
                                              HttpServletRequest request,
                                              @UserAID @PathVariable String aid) {
        User user = userService.getUserByAssetHolderId(aid);
        user.setPassword(null);
        user.setPermissionConfig(QueryTool.getUserPermissions(null, aid));
        return new ResponseBody(Code.SELECT_OK, user);
    }

    // NOTICE: No Post Mapping. A common user cannot insert new users, unless they access login controller to register

    @PutMapping("/user/uid/{uid}")
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

    @PutMapping("/user/aid/{aid}")
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

    @DeleteMapping("/user/uid/{uid}")
    public ResponseBody userDeleteMyProfileWithUID(HttpServletResponse response,
                                                   HttpServletRequest request,
                                                   @UserUID @PathVariable String uid) {
        userService.deleteUserByUserIds(new String[]{uid});
        return new ResponseBody(Code.DELETE_OK, null);
    }

    @DeleteMapping("/user/aid/{aid}")
    public ResponseBody userDeleteMyProfileWithAID(HttpServletResponse response,
                                                   HttpServletRequest request,
                                                   @UserAID @PathVariable String aid) {
        userService.deleteUserByAssetHolderIds(new String[]{aid});
        return new ResponseBody(Code.DELETE_OK, null);
    }

    @GetMapping("/user/uid/{uid}/permission")
    public ResponseBody userGetMyPermissionByUID(HttpServletResponse response,
                                                 HttpServletRequest request,
                                                 @UserUID @PathVariable String uid) {
        return new ResponseBody(Code.SELECT_OK, QueryTool.getUserPermissions(uid, null));
    }

    /* --------- ********************* --------- */
    /* --------- interfaces for admins --------- */
    /* --------- ********************* --------- */

    @GetMapping("/admin/user/all")
    public ResponseBody getAllUsers(@RequestParam(required = false) List<String> orderList,
                                    @RequestParam(required = false) Integer limit,
                                    @RequestParam(required = false) Integer offset) {
        return new ResponseBody(Code.SELECT_OK, userService.getAllUsersWithAssetHolder(null, QueryTool.getOrderList(orderList), limit, offset));
    }

    @PostMapping("/admin/user/all/search")
    public ResponseBody getAllUsers(@RequestBody FilterDTO filter) {
        if (!filter.hasOrderList() && (filter.hasLimit() || filter.hasOffset())) {
            throw new SpExceptions.BadRequestException("Pagination parameters specified without order list.");
        }

        return new ResponseBody(Code.SELECT_OK, userService.getAllUsersWithAssetHolder(
                filter.getFilters(),
                QueryTool.getOrderList(filter.getOrderList()),
                filter.getLimit(),
                filter.getOffset()
        ));
    }

    @GetMapping("/admin/user/unauthorised")
    public ResponseBody getAllUnauthorisedUsersWithAssetHolder(@RequestParam(required = false) List<String> orderList,
                                                               @RequestParam(required = false) Integer limit,
                                                               @RequestParam(required = false) Integer offset) {
        return new ResponseBody(Code.SELECT_OK, userService.getAllUnauthorisedUsersWithAssetHolder(null, QueryTool.getOrderList(orderList), limit, offset));
    }

    @PostMapping("/admin/user/unauthorised/search")
    public ResponseBody getAllUnauthorisedUsersWithAssetHolder(@RequestBody FilterDTO filter) {
        if (!filter.hasOrderList() && (filter.hasLimit() || filter.hasOffset())) {
            throw new SpExceptions.BadRequestException("Pagination parameters specified without order list.");
        }

        return new ResponseBody(Code.SELECT_OK, userService.getAllUnauthorisedUsersWithAssetHolder(
                filter.getFilters(),
                QueryTool.getOrderList(filter.getOrderList()),
                filter.getLimit(),
                filter.getOffset()
        ));
    }

    @GetMapping("/admin/user/with-asset-ids")
    public ResponseBody getAllAssetHoldersWithAssetIds(@RequestParam(required = false) List<String> orderList,
                                                       @RequestParam(required = false) Integer limit,
                                                       @RequestParam(required = false) Integer offset) {
        return new ResponseBody(Code.SELECT_OK, userService.getAllAssetHoldersWithAssetIds(null, QueryTool.getOrderList(orderList), limit, offset));
    }

    @PostMapping("/admin/user/with-asset-ids/search")
    public ResponseBody getAllAssetHoldersWithAssetIds(@RequestBody FilterDTO filter) {
        if (!filter.hasOrderList() && (filter.hasLimit() || filter.hasOffset())) {
            throw new SpExceptions.BadRequestException("Pagination parameters specified without order list.");
        }

        return new ResponseBody(Code.SELECT_OK, userService.getAllAssetHoldersWithAssetIds(
                filter.getFilters(),
                QueryTool.getOrderList(filter.getOrderList()),
                filter.getLimit(),
                filter.getOffset()
        ));
    }

    /**
     * e.g. "/user/accumulate?function=count"
     * <br><br>
     * BE AWARE: This SQL Statement is weak to SQL injection, do pass verified parameter in!
     */
    @GetMapping("/admin/user/accumulate")
    public ResponseBody getAllUsersWithAccumulator(@RequestParam String function,
                                                   @RequestParam String column,
                                                   @RequestParam(required = false) List<String> orderList,
                                                   @RequestParam(required = false) Integer limit,
                                                   @RequestParam(required = false) Integer offset) {
        return new ResponseBody(Code.SELECT_OK, userService.getAllUsersWithAccumulator(function, column, null, QueryTool.getOrderList(orderList), limit, offset));
    }

    @PostMapping("/admin/user/accumulate/search")
    public ResponseBody getAllUsersWithAccumulator(@RequestParam String function,
                                                   @RequestParam String column,
                                                   @RequestBody FilterDTO filter) {
        if (!filter.hasOrderList() && (filter.hasLimit() || filter.hasOffset())) {
            throw new SpExceptions.BadRequestException("Pagination parameters specified without order list.");
        }

        return new ResponseBody(Code.SELECT_OK, userService.getAllUsersWithAccumulator(function, column,
                filter.getFilters(),
                QueryTool.getOrderList(filter.getOrderList()),
                filter.getLimit(),
                filter.getOffset()
        ));
    }

    @GetMapping("/admin/user/count")
    public ResponseBody countUsersByFilter(@RequestBody FilterDTO filter) {
        return new ResponseBody(Code.SELECT_OK, userService.countUsersWithFilter(filter.getFilters()));
    }

    @GetMapping("/admin/user/uid/{id}")
    public ResponseBody getUserByUserId(@PathVariable String id) {
        User user = userService.getUserByUserId(id);
        user.setPassword(null);
        return new ResponseBody(Code.SELECT_OK, user);
    }

    @GetMapping("/admin/user/aid/{id}")
    public ResponseBody getUserByAssetHolderId(@PathVariable String id) {
        User user = userService.getUserByAssetHolderId(id);
        user.setPassword(null);
        return new ResponseBody(Code.SELECT_OK, user);
    }

    @PostMapping("/admin/user")
    public ResponseBody insertUsers(@RequestBody List<User> list) {
        for (User u : list) {
            userService.insertUser(u);
        }
        return new ResponseBody(Code.INSERT_OK, null);
    }

    @PutMapping("/admin/user")
    public ResponseBody updateUsers(@RequestBody List<User> list) {
        for (User u : list) {
            userService.updateUser(u);
        }
        return new ResponseBody(Code.UPDATE_OK, null);
    }

    // TODO: Consider soft delete.
    @DeleteMapping("/admin/user")
    public ResponseBody deleteUserByUserIds(@RequestBody Map<String, Object> body) {
        List<String> ids = (List<String>) body.get("ids");
        return new ResponseBody(Code.DELETE_OK, userService.deleteUserByUserIds(ids));
    }
}
