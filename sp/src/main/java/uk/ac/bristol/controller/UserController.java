package uk.ac.bristol.controller;

import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.advice.UserAID;
import uk.ac.bristol.advice.UserIdentificationAIDExecution;
import uk.ac.bristol.advice.UserIdentificationUIDExecution;
import uk.ac.bristol.advice.UserUID;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.FilterDTO;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.pojo.UserGroupDTO;
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

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /* --------- *************************** --------- */
    /* --------- interfaces for common users --------- */
    /* --------- *************************** --------- */

    @UserIdentificationUIDExecution
    @GetMapping("/user/uid/{uid}")
    public ResponseBody getMyProfileByUID(HttpServletResponse response,
                                          HttpServletRequest request,
                                          @UserUID @PathVariable String uid) {
        User user = userService.getUserByUserId(uid);
        user.setPassword(null);
        user.setPermissionConfig(QueryTool.getUserPermissions(uid, null));
        return new ResponseBody(Code.SELECT_OK, user);
    }

    @UserIdentificationAIDExecution
    @GetMapping("/admin/user/group")
    public ResponseBody getGroupedUsers(@RequestParam String groupBy) {
        List<UserGroupDTO> groupedUsers = userService.getGroupedUsers(groupBy);
        return new ResponseBody(Code.SELECT_OK, groupedUsers);
    }

    @GetMapping("/user/aid/{aid}")
    public ResponseBody getMyProfileByAID(HttpServletResponse response,
                                          HttpServletRequest request,
                                          @UserAID @PathVariable String aid) {
        User user = userService.getUserByAssetHolderId(aid);
        user.setPassword(null);
        user.setPermissionConfig(QueryTool.getUserPermissions(null, aid));
        return new ResponseBody(Code.SELECT_OK, user);
    }

    @UserIdentificationUIDExecution
    @RequestMapping(value = "/user/uid/{uid}", method = RequestMethod.HEAD)
    public void headMyLastModifiedByUID(HttpServletResponse response,
                                        HttpServletRequest request,
                                        @UserUID @PathVariable String uid,
                                        @RequestParam(value = "time", required = true) Long timestamp) {
        boolean b = userService.compareUserLastModified(uid, timestamp);
        response.setHeader("last-modified", Boolean.toString(b));
        if (b) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    @UserIdentificationAIDExecution
    @RequestMapping(value = "/user/aid/{aid}", method = RequestMethod.HEAD)
    public void headMyLastModifiedByAID(HttpServletResponse response,
                                        HttpServletRequest request,
                                        @UserAID @PathVariable String aid,
                                        @RequestParam(value = "time", required = true) Long timestamp) {
        boolean b = userService.compareUserLastModified(userService.getUserByAssetHolderId(aid).getId(), timestamp);
        response.setHeader("last-modified", Boolean.toString(b));
        if (b) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    // NOTICE: No Post Mapping. A common user cannot insert new users, unless they access login controller to register

    @UserIdentificationUIDExecution
    @PutMapping("/user/uid/{uid}")
    public ResponseBody updateMyProfileWithUID(HttpServletResponse response,
                                               HttpServletRequest request,
                                               @UserUID @PathVariable String uid,
                                               @RequestBody User user) {
        if (!QueryTool.getUserPermissions(uid, null).getCanUpdateProfile()) {
            throw new SpExceptions.ForbiddenException("The user is not allowed to update profile");
        }
        userService.updateUser(user);
        return new ResponseBody(Code.UPDATE_OK, null);
    }

    @UserIdentificationAIDExecution
    @PutMapping("/user/aid/{aid}")
    public ResponseBody updateMyProfileWithAID(HttpServletResponse response,
                                               HttpServletRequest request,
                                               @UserAID @PathVariable String aid,
                                               @RequestBody User user) {
        if (!QueryTool.getUserPermissions(null, aid).getCanUpdateProfile()) {
            throw new SpExceptions.ForbiddenException("The user is not allowed to update profile");
        }
        userService.updateUser(user);
        return new ResponseBody(Code.UPDATE_OK, null);
    }

    @UserIdentificationUIDExecution
    @DeleteMapping("/user/uid/{uid}")
    public ResponseBody deleteMyProfileWithUID(HttpServletResponse response,
                                               HttpServletRequest request,
                                               @UserUID @PathVariable String uid) {
        userService.deleteUserByUserIds(new String[]{uid});
        return new ResponseBody(Code.DELETE_OK, null);
    }

    @UserIdentificationAIDExecution
    @DeleteMapping("/user/aid/{aid}")
    public ResponseBody deleteMyProfileWithAID(HttpServletResponse response,
                                               HttpServletRequest request,
                                               @UserAID @PathVariable String aid) {
        userService.deleteUserByAssetHolderIds(new String[]{aid});
        return new ResponseBody(Code.DELETE_OK, null);
    }

    @UserIdentificationUIDExecution
    @GetMapping("/user/uid/{uid}/permission")
    public ResponseBody getMyPermissionByUID(HttpServletResponse response,
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
        FilterDTO filter = new FilterDTO(limit);
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, userService.getAllUsersWithAssetHolder(
                null,
                QueryTool.getOrderList(orderList),
                filter.getLimit(),
                offset
        ), message);
    }

    @PostMapping("/admin/user/all/search")
    public ResponseBody getAllUsers(@RequestBody FilterDTO filter) {
        if (!filter.hasOrderList() && (filter.hasLimit() || filter.hasOffset())) {
            throw new SpExceptions.BadRequestException("Pagination parameters specified without order list.");
        }
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, userService.getAllUsersWithAssetHolder(
                filter.getFilters(),
                QueryTool.getOrderList(filter.getOrderList()),
                filter.getLimit(),
                filter.getOffset()
        ), message);
    }

    @GetMapping("/admin/user/unauthorised")
    public ResponseBody getAllUnauthorisedUsersWithAssetHolder(@RequestParam(required = false) List<String> orderList,
                                                               @RequestParam(required = false) Integer limit,
                                                               @RequestParam(required = false) Integer offset) {
        FilterDTO filter = new FilterDTO(limit);
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, userService.getAllUnauthorisedUsersWithAssetHolder(
                null,
                QueryTool.getOrderList(orderList),
                filter.getLimit(), offset
        ), message);
    }

    @PostMapping("/admin/user/unauthorised/search")
    public ResponseBody getAllUnauthorisedUsersWithAssetHolder(@RequestBody FilterDTO filter) {
        if (!filter.hasOrderList() && (filter.hasLimit() || filter.hasOffset())) {
            throw new SpExceptions.BadRequestException("Pagination parameters specified without order list.");
        }
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, userService.getAllUnauthorisedUsersWithAssetHolder(
                filter.getFilters(),
                QueryTool.getOrderList(filter.getOrderList()),
                filter.getLimit(),
                filter.getOffset()
        ), message);
    }

    @GetMapping("/admin/user/with-asset-ids")
    public ResponseBody getAllAssetHoldersWithAssetIds(@RequestParam(required = false) List<String> orderList,
                                                       @RequestParam(required = false) Integer limit,
                                                       @RequestParam(required = false) Integer offset) {
        FilterDTO filter = new FilterDTO(limit);
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, userService.getAllAssetHoldersWithAssetIds(
                null,
                QueryTool.getOrderList(orderList),
                filter.getLimit(),
                offset
        ), message);
    }

    @PostMapping("/admin/user/with-asset-ids/search")
    public ResponseBody getAllAssetHoldersWithAssetIds(@RequestBody FilterDTO filter) {
        if (!filter.hasOrderList() && (filter.hasLimit() || filter.hasOffset())) {
            throw new SpExceptions.BadRequestException("Pagination parameters specified without order list.");
        }
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, userService.getAllAssetHoldersWithAssetIds(
                filter.getFilters(),
                QueryTool.getOrderList(filter.getOrderList()),
                filter.getLimit(),
                filter.getOffset()
        ), message);
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
        FilterDTO filter = new FilterDTO(limit);
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, userService.getAllUsersWithAccumulator(
                function, column,
                null,
                QueryTool.getOrderList(orderList),
                filter.getLimit(),
                offset
        ), message);
    }

    @PostMapping("/admin/user/accumulate/search")
    public ResponseBody getAllUsersWithAccumulator(@RequestParam String function,
                                                   @RequestParam String column,
                                                   @RequestBody FilterDTO filter) {
        if (!filter.hasOrderList() && (filter.hasLimit() || filter.hasOffset())) {
            throw new SpExceptions.BadRequestException("Pagination parameters specified without order list.");
        }
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, userService.getAllUsersWithAccumulator(function, column,
                filter.getFilters(),
                QueryTool.getOrderList(filter.getOrderList()),
                filter.getLimit(),
                filter.getOffset()
        ), message);
    }

    @PostMapping("/admin/user/count")
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

    @RequestMapping(value = "/admin/user/uid/{uid}", method = RequestMethod.HEAD)
    public void headUserLastModifiedByUID(HttpServletResponse response,
                                          HttpServletRequest request,
                                          @UserUID @PathVariable String uid,
                                          @RequestParam(value = "time", required = true) Long timestamp) {
        boolean b = userService.compareUserLastModified(uid, timestamp);
        response.setHeader("last-modified", Boolean.toString(b));
        if (b) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    @RequestMapping(value = "/admin/user/aid/{aid}", method = RequestMethod.HEAD)
    public void headUserMyLastModifiedByAID(HttpServletResponse response,
                                            HttpServletRequest request,
                                            @UserAID @PathVariable String aid,
                                            @RequestParam(value = "time", required = true) Long timestamp) {
        boolean b = userService.compareUserLastModified(userService.getUserByAssetHolderId(aid).getId(), timestamp);
        response.setHeader("last-modified", Boolean.toString(b));
        if (b) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    @PostMapping("/admin/user")
    public ResponseBody insertUser(@RequestBody User user) {
        return new ResponseBody(Code.INSERT_OK, userService.insertUser(user));
    }

    @PostMapping("/admin/user/batch")
    public ResponseBody insertUserBatch(@RequestBody List<User> list) {
        return new ResponseBody(Code.INSERT_OK, userService.insertUserBatch(list));
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
