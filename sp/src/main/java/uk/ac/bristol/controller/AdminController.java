package uk.ac.bristol.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.pojo.AssetType;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.pojo.Warning;
import uk.ac.bristol.service.AssetService;
import uk.ac.bristol.service.UserService;
import uk.ac.bristol.service.WarningService;
import uk.ac.bristol.util.QueryTool;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final UserService userService;
    private final AssetService assetService;
    private final WarningService warningService;

    public AdminController(UserService userService, AssetService assetService, WarningService warningService) {
        this.userService = userService;
        this.assetService = assetService;
        this.warningService = warningService;
    }

    /**
     * Instant Messaging: Client receives a message only when it's online.
     * To improve: Use Message Queue (Kafka / RabbitMQ / RocketMQ) with database (Redis)
     * <br>
     * <a href="https://docs.spring.io/spring-framework/reference/web/websocket/stomp.html">Spring Stomp</a>
     */
//    @PostMapping("/notify")
//    public ResponseBody sendNotification(@RequestBody Map<String, String> body) {
//        String m = body.get("message");
//        messagingTemplate.convertAndSend("/topic/notify", m);
//        return new ResponseBody(Code.SUCCESS, null, "Notification sent");
//    }
    @PostMapping("/notify")
    public ResponseBody sendNotifications(@RequestBody Map<String, String> body) {
        Long warningId = Long.valueOf(body.get("warningId"));
        String assetTypeId = body.get("assetTypeId");
        Integer messageId = Integer.valueOf(body.get("messageId"));
        List<Warning> warning = warningService.getWarningById(warningId);
        AssetType type = ((Supplier<AssetType>) () -> {
            List<AssetType> types = assetService.getAllAssetTypes(null, null, null);
            for (AssetType t : types) {
                if (t.getId().equals(assetTypeId)) {
                    return t;
                }
            }
            return null;
        }).get();
        String message = ((Supplier<String>) () -> {
            List<Map<String, Object>> list = warningService.getAllNotificationTemplates();
            for (Map<String, Object> map : list) {
                if (Objects.equals((Integer) map.get("id"), messageId)) {
                    return map.get("message").toString();
                }
            }
            return null;
        }).get();
        return new ResponseBody(Code.SUCCESS, warningService.sendNotifications(warning.get(0), type, message));
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
     * e.g. "/user/accumulate?function=count"
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

    @GetMapping("/asset")
    public ResponseBody getAllAssetsWithWarnings(@RequestParam(required = false) List<String> orderList,
                                                 @RequestParam(required = false) Integer limit,
                                                 @RequestParam(required = false) Integer offset) {
        return new ResponseBody(Code.SELECT_OK, assetService.getAllAssetsWithWarnings(QueryTool.getOrderList(orderList), limit, offset));
    }

    @GetMapping("/asset/{assetId}")
    public ResponseBody getAssetById(@PathVariable String assetId) {
        return new ResponseBody(Code.SELECT_OK, assetService.getAssetWithWarningsById(assetId));
    }

}
