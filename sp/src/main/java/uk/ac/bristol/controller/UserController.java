package uk.ac.bristol.controller;

import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.advice.PostSearchEndpoint;
import uk.ac.bristol.advice.UserIdentificationExecution;
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

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /* --------- *************************** --------- */
    /* --------- interfaces for common users --------- */
    /* --------- *************************** --------- */

    @UserIdentificationExecution
    @GetMapping("/user/uid/{uid}")
    public ResponseBody getMyProfileByUID(HttpServletResponse response,
                                          HttpServletRequest request,
                                          @UserUID @PathVariable String uid) {
        return new ResponseBody(Code.SELECT_OK, userService.getUserByUserId(uid));
    }

    @UserIdentificationExecution
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

    // NOTICE: No Post Mapping. A common user cannot insert new users, unless they access login controller to register

    @UserIdentificationExecution
    @PutMapping("/user/uid/{uid}")
    public ResponseBody updateMyProfileWithUID(HttpServletResponse response,
                                               HttpServletRequest request,
                                               @UserUID @PathVariable String uid,
                                               @RequestBody User user) {
        if (!QueryTool.getAccessControlGroupByUserId(uid).getCanUpdateProfile()) {
            throw new SpExceptions.ForbiddenException("The user is not allowed to update profile");
        }
        userService.updateUser(user);
        return new ResponseBody(Code.UPDATE_OK, null);
    }

    @UserIdentificationExecution
    @DeleteMapping("/user/uid/{uid}")
    public ResponseBody deleteMyProfileWithUID(HttpServletResponse response,
                                               HttpServletRequest request,
                                               @UserUID @PathVariable String uid) {
        userService.deleteUserByUserIds(List.of(uid));
        return new ResponseBody(Code.DELETE_OK, null);
    }

    @UserIdentificationExecution
    @GetMapping("/user/uid/{uid}/access-group")
    public ResponseBody getMyAccessControlGroupByUID(HttpServletResponse response,
                                                     HttpServletRequest request,
                                                     @UserUID @PathVariable String uid) {
        return new ResponseBody(Code.SELECT_OK, QueryTool.getAccessControlGroupByUserId(uid));
    }

    /* --------- ********************* --------- */
    /* --------- interfaces for admins --------- */
    /* --------- ********************* --------- */

    @GetMapping("/admin/user/all")
    public ResponseBody getAllUsers(@RequestParam(required = false) List<String> orderList,
                                    @RequestParam(required = false) Integer limit,
                                    @RequestParam(required = false) Integer offset) {
        FilterDTO filter = new FilterDTO(limit, offset);
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, userService.getUsers(
                null,
                QueryTool.getOrderList(orderList),
                filter.getLimit(),
                offset
        ), message);
    }

    @PostSearchEndpoint
    @PostMapping("/admin/user/all/search")
    public ResponseBody getAllUsers(@RequestBody FilterDTO filter) {
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, userService.getCursoredUsers(
                filter.getLastRowId(),
                filter.getFilters(),
                QueryTool.getOrderList(filter.getOrderList()),
                filter.getLimit(),
                filter.getOffset()
        ), message);
    }

    @GetMapping("/admin/user/unauthorised")
    public ResponseBody getAllUnauthorisedUsers(@RequestParam(required = false) List<String> orderList,
                                                @RequestParam(required = false) Integer limit,
                                                @RequestParam(required = false) Integer offset) {
        FilterDTO filter = new FilterDTO(limit, offset);
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, userService.getUnauthorisedUsers(
                null,
                QueryTool.getOrderList(orderList),
                filter.getLimit(), offset
        ), message);
    }

    @PostSearchEndpoint
    @PostMapping("/admin/user/unauthorised/search")
    public ResponseBody getAllUnauthorisedUsers(@RequestBody FilterDTO filter) {
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, userService.getUnauthorisedUsers(
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
        FilterDTO filter = new FilterDTO(limit, offset);
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, userService.getUsersWithAccumulator(
                function, column,
                null,
                QueryTool.getOrderList(orderList),
                filter.getLimit(),
                offset
        ), message);
    }

    @PostSearchEndpoint
    @PostMapping("/admin/user/accumulate/search")
    public ResponseBody getAllUsersWithAccumulator(@RequestParam String function,
                                                   @RequestParam String column,
                                                   @RequestBody FilterDTO filter) {
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, userService.getCursoredUsersWithAccumulator(
                function, column,
                filter.getLastRowId(),
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
        return new ResponseBody(Code.SELECT_OK, user);
    }

    @RequestMapping(value = "/admin/user/uid/{uid}", method = RequestMethod.HEAD)
    public void headUserLastModifiedByUID(HttpServletResponse response,
                                          @PathVariable String uid,
                                          @RequestParam(value = "time", required = true) Long timestamp) {
        boolean b = userService.compareUserLastModified(uid, timestamp);
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
        return new ResponseBody(Code.UPDATE_OK, userService.updateUserBatch(list));
    }

    // TODO: Consider soft delete.
    @DeleteMapping("/admin/user")
    public ResponseBody deleteUserByUserIds(@RequestBody Map<String, Object> body) {
        List<String> ids = (List<String>) body.get("ids");
        return new ResponseBody(Code.DELETE_OK, userService.deleteUserByUserIds(ids));
    }
}
