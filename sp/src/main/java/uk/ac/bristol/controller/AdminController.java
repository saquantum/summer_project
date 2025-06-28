package uk.ac.bristol.controller;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.pojo.AssetHolder;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.service.UserService;
import uk.ac.bristol.util.JwtUtil;
import uk.ac.bristol.util.QueryTool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Instant Messaging: Client receives a message only when it's online.
     * To improve: Use Message Queue (Kafka / RabbitMQ / RocketMQ) with database (Redis)
     * <br>
     * <a href="https://docs.spring.io/spring-framework/reference/web/websocket/stomp.html">Spring Stomp</a>
     */
    @PostMapping("/notify")
    public ResponseBody sendNotification(@RequestBody Map<String, String> body) {
        String m = body.get("message");
        messagingTemplate.convertAndSend("/topic/notify", m);
        return new ResponseBody(Code.SUCCESS, null, "Notification sent");
    }

    @GetMapping("/as/{id}")
    public ResponseBody asUser(HttpServletResponse response, HttpServletRequest request, @PathVariable String id) throws IOException {
        Claims claims = JwtUtil.parseJWT(JwtUtil.getJWTFromCookie(request, response));
        User user = userService.getUserByAssetHolderId(id, null, null, null);
        if(user == null){
            throw new RuntimeException("User with asset holder id is not found");
        }
        claims.put("assetHolderId", id);
        JwtUtil.bindJWTAsCookie(response, JwtUtil.generateJWT(claims));
        return new ResponseBody(Code.SUCCESS, null, "You are in proxy mode as user id " + id);
    }

    @GetMapping("/as/clear")
    public ResponseBody clearProxy(HttpServletResponse response, HttpServletRequest request) throws IOException {
        Claims claims = JwtUtil.parseJWT(JwtUtil.getJWTFromCookie(request, response));
        claims.remove("assetHolderId");
        JwtUtil.bindJWTAsCookie(response, JwtUtil.generateJWT(claims));
        return new ResponseBody(Code.SUCCESS, null, "Proxy mode cleared.");
    }

    @GetMapping("/user/all")
    public ResponseBody getAllUsers(@RequestParam(required = false) List<String> orderList,
                                    @RequestParam(required = false) Integer limit,
                                    @RequestParam(required = false) Integer offset) {
        return new ResponseBody(Code.SELECT_OK, userService.getAllUsersWithAssetHolder(QueryTool.getOrderList(orderList), limit, offset));
    }

    @GetMapping("/user/unauthorised")
    public ResponseBody getAllUnauthorisedUsersWithAssetHolder(@RequestParam(required = false) List<String> orderList,
                                                               @RequestParam(required = false) Integer limit,
                                                               @RequestParam(required = false) Integer offset) {
        return new ResponseBody(Code.SELECT_OK, userService.getAllUnauthorisedUsersWithAssetHolder(QueryTool.getOrderList(orderList), limit, offset));
    }

    @GetMapping("/user/with-asset-ids")
    public ResponseBody getAllAssetHoldersWithAssetIds(@RequestParam(required = false) List<String> orderList,
                                                       @RequestParam(required = false) Integer limit,
                                                       @RequestParam(required = false) Integer offset) {
        return new ResponseBody(Code.SELECT_OK, userService.getAllAssetHoldersWithAssetIds(QueryTool.getOrderList(orderList), limit, offset));
    }

    /**
     * e.g. "/user/accumulate/count/{column}"
     * <br><br>
     * BE AWARE: This SQL Statement is weak to SQL injection, do pass verified parameter in!
     */
    @GetMapping("/user/accumulate")
    public ResponseBody getAllUsersWithAccumulator(@RequestParam String function,
                                                   @RequestParam String column,
                                                   @RequestParam(required = false) List<String> orderList,
                                                   @RequestParam(required = false) Integer limit,
                                                   @RequestParam(required = false) Integer offset) {
        return new ResponseBody(Code.SELECT_OK, userService.getAllUsersWithAccumulator(function, column, QueryTool.getOrderList(orderList), limit, offset));
    }

    @GetMapping("/user/uid/{id}")
    public ResponseBody getUserByUserId(@PathVariable String id,
                                        @RequestParam(required = false) List<String> orderList,
                                        @RequestParam(required = false) Integer limit,
                                        @RequestParam(required = false) Integer offset) {

        User user = userService.getUserByUserId(id, QueryTool.getOrderList(orderList), limit, offset);
        user.setPassword(null);
        return new ResponseBody(Code.SELECT_OK, user);
    }

    @GetMapping("/user/aid/{id}")
    public ResponseBody getUserByAssetHolderId(@PathVariable String id,
                                               @RequestParam(required = false) List<String> orderList,
                                               @RequestParam(required = false) Integer limit,
                                               @RequestParam(required = false) Integer offset) {
        User user = userService.getUserByAssetHolderId(id, QueryTool.getOrderList(orderList), limit, offset);
        user.setPassword(null);
        return new ResponseBody(Code.SELECT_OK, user);
    }

    @PostMapping("/user")
    public ResponseBody insertUsers(@RequestBody List<User> list) {
        for (User u : list) {
            userService.insertUser(u);
        }
        return new ResponseBody(Code.INSERT_OK, null);
    }

    @PutMapping("/user")
    public ResponseBody updateUsers(@RequestBody List<User> list) {
        for (User u : list) {
            userService.updateUser(u);
        }
        return new ResponseBody(Code.UPDATE_OK, null);
    }

    // TODO: Consider soft delete.
    @DeleteMapping("/user")
    public ResponseBody deleteUserByUserIds(@RequestBody Map<String, Object> body) {
        List<String> ids = (List<String>) body.get("ids");
        return new ResponseBody(Code.DELETE_OK, userService.deleteUserByUserIds(ids));
    }
}
