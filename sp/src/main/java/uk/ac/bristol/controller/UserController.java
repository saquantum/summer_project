package uk.ac.bristol.controller;

import io.jsonwebtoken.Claims;
import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.service.UserService;
import uk.ac.bristol.util.JwtUtil;
import uk.ac.bristol.util.QueryTool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseBody getUserInfo(HttpServletResponse response, HttpServletRequest request) throws Exception {
        String token = JwtUtil.getJWTFromCookie(request, response);
        Claims claims = JwtUtil.parseJWT(token);
        Map<String, Object> data = new HashMap<>();
        boolean isAdmin = claims.get("isAdmin", Boolean.class);
        data.put("isAdmin", isAdmin);
        // for user, must get asset holder id, or something has gone wrong
        if (!isAdmin) {
            data.put("id", claims.get("assetHolderId", String.class));
            return new ResponseBody(Code.SUCCESS, data);
        }
        // for admin, two possibilities:
        else {
            // 1. with proxy id
            if (claims.containsKey("asUserId")) {
                data.put("id", claims.get("asUserId", String.class));
                if (claims.containsKey("asUserInAssetId")) {
                    data.put("asUserInAssetId", claims.get("asUserInAssetId", String.class));
                }
                return new ResponseBody(Code.SUCCESS, data);
            }
            // 2. without proxy id
            else {
                data.put("id", -1);
                return new ResponseBody(Code.SUCCESS, data);
            }
        }
    }

    // for safety considerations, the user should not see password.
    @GetMapping("/uid/{id}")
    public ResponseBody getUserByUserId(@PathVariable String id,
                                        @RequestParam(required = false) List<String> orderList,
                                        @RequestParam(required = false) Integer limit,
                                        @RequestParam(required = false) Integer offset) {
        User user = userService.getUserByUserId(id, QueryTool.getOrderList(orderList), limit, offset);
        user.setPassword(null);
        return new ResponseBody(Code.SELECT_OK, user);
    }

    @GetMapping("/aid/{id}")
    public ResponseBody getUserByAssetHolderId(@PathVariable String id,
                                               @RequestParam(required = false) List<String> orderList,
                                               @RequestParam(required = false) Integer limit,
                                               @RequestParam(required = false) Integer offset) {
        User user = userService.getUserByAssetHolderId(id, QueryTool.getOrderList(orderList), limit, offset);
        user.setPassword(null);
        return new ResponseBody(Code.SELECT_OK, user);
    }

    // NOTICE: No Post Mapping. A common user cannot insert new users, unless they access login controller to register

    // TODO: A common user can only update itself, involving identity check with jwt.
    @PutMapping()
    public ResponseBody updateUser(@RequestBody User user) {
        return new ResponseBody(Code.UPDATE_OK, userService.updateUser(user));
    }

    // TODO: A common user can only delete itself, involving identity check with jwt.
    // TODO: Consider soft delete: mark the user as deleted and keep all configs.
    @DeleteMapping("/aid/{id}")
    public ResponseBody deleteUserByAssetHolderId(@PathVariable String id) {
        return new ResponseBody(Code.DELETE_OK, userService.deleteUserByAssetHolderIds(new String[]{id}));
    }

}
