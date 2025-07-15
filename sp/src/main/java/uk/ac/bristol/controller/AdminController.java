package uk.ac.bristol.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.FilterDTO;
import uk.ac.bristol.pojo.PermissionConfig;
import uk.ac.bristol.pojo.Template;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.service.*;
import uk.ac.bristol.util.QueryTool;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final UserService userService;
    private final AssetService assetService;
    private final WarningService warningService;
    private final MetaDataService metaDataService;
    private final PermissionConfigService permissionConfigService;

    public AdminController(UserService userService, AssetService assetService, WarningService warningService, MetaDataService metaDataService, PermissionConfigService permissionConfigService) {
        this.userService = userService;
        this.assetService = assetService;
        this.warningService = warningService;
        this.metaDataService = metaDataService;
        this.permissionConfigService = permissionConfigService;
    }

    @GetMapping("/user/all")
    public ResponseBody getAllUsers(@RequestParam(required = false) List<String> orderList,
                                    @RequestParam(required = false) Integer limit,
                                    @RequestParam(required = false) Integer offset) {
        return new ResponseBody(Code.SELECT_OK, userService.getAllUsersWithAssetHolder(null, QueryTool.getOrderList(orderList), limit, offset));
    }

    @PostMapping("/user/all/search")
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

    @GetMapping("/user/unauthorised")
    public ResponseBody getAllUnauthorisedUsersWithAssetHolder(@RequestParam(required = false) List<String> orderList,
                                                               @RequestParam(required = false) Integer limit,
                                                               @RequestParam(required = false) Integer offset) {
        return new ResponseBody(Code.SELECT_OK, userService.getAllUnauthorisedUsersWithAssetHolder(null, QueryTool.getOrderList(orderList), limit, offset));
    }

    @PostMapping("/user/unauthorised/search")
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

    @GetMapping("/user/with-asset-ids")
    public ResponseBody getAllAssetHoldersWithAssetIds(@RequestParam(required = false) List<String> orderList,
                                                       @RequestParam(required = false) Integer limit,
                                                       @RequestParam(required = false) Integer offset) {
        return new ResponseBody(Code.SELECT_OK, userService.getAllAssetHoldersWithAssetIds(null, QueryTool.getOrderList(orderList), limit, offset));
    }

    @PostMapping("/user/with-asset-ids/search")
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
    @GetMapping("/user/accumulate")
    public ResponseBody getAllUsersWithAccumulator(@RequestParam String function,
                                                   @RequestParam String column,
                                                   @RequestParam(required = false) List<String> orderList,
                                                   @RequestParam(required = false) Integer limit,
                                                   @RequestParam(required = false) Integer offset) {
        return new ResponseBody(Code.SELECT_OK, userService.getAllUsersWithAccumulator(function, column, null, QueryTool.getOrderList(orderList), limit, offset));
    }

    @PostMapping("/user/accumulate/search")
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

    @GetMapping("/user/count")
    public ResponseBody countUsersByFilter(@RequestBody FilterDTO filter) {
        return new ResponseBody(Code.SELECT_OK, userService.countUsersWithFilter(filter.getFilters()));
    }

    @GetMapping("/user/uid/{id}")
    public ResponseBody getUserByUserId(@PathVariable String id) {
        User user = userService.getUserByUserId(id);
        user.setPassword(null);
        return new ResponseBody(Code.SELECT_OK, user);
    }

    @GetMapping("/user/aid/{id}")
    public ResponseBody getUserByAssetHolderId(@PathVariable String id) {
        User user = userService.getUserByAssetHolderId(id);
        user.setPassword(null);
        return new ResponseBody(Code.SELECT_OK, user);
    }

    @GetMapping("/metadata")
    public ResponseBody getAllMetaData() {
        return new ResponseBody(Code.SELECT_OK, metaDataService.getAllMetaData());
    }

    @GetMapping("/metadata/{tableName}")
    public ResponseBody getMetaDataByTableName(@PathVariable String tableName) {
        return new ResponseBody(Code.SELECT_OK, metaDataService.getMetaDataByTableName(tableName));
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
        return new ResponseBody(Code.SELECT_OK, assetService.getAllAssetsWithWarnings(null, QueryTool.getOrderList(orderList), limit, offset));
    }

    @PostMapping("/asset/search")
    public ResponseBody getAllAssetsWithWarnings(@RequestBody FilterDTO filter) {
        if (!filter.hasOrderList() && (filter.hasLimit() || filter.hasOffset())) {
            throw new SpExceptions.BadRequestException("Pagination parameters specified without order list.");
        }

        return new ResponseBody(Code.SELECT_OK, assetService.getAllAssetsWithWarnings(
                filter.getFilters(),
                QueryTool.getOrderList(filter.getOrderList()),
                filter.getLimit(),
                filter.getOffset()
        ));
    }

    @GetMapping("/asset/{assetId}")
    public ResponseBody getAssetById(@PathVariable String assetId) {
        return new ResponseBody(Code.SELECT_OK, assetService.getAssetWithWarningsById(assetId));
    }

    @GetMapping("/template")
    public ResponseBody getAllTemplates(@RequestParam(required = false) List<String> orderList,
                                        @RequestParam(required = false) Integer limit,
                                        @RequestParam(required = false) Integer offset) {
        return new ResponseBody(Code.SELECT_OK, warningService.getAllNotificationTemplates(null, QueryTool.getOrderList(orderList), limit, offset));
    }

    @PostMapping("/template/search")
    public ResponseBody getAllTemplates(@RequestBody FilterDTO filter) {
        if (!filter.hasOrderList() && (filter.hasLimit() || filter.hasOffset())) {
            throw new SpExceptions.BadRequestException("Pagination parameters specified without order list.");
        }

        return new ResponseBody(Code.SELECT_OK, warningService.getAllNotificationTemplates(
                filter.getFilters(),
                QueryTool.getOrderList(filter.getOrderList()),
                filter.getLimit(),
                filter.getOffset()
        ));
    }

    @GetMapping("/template/type")
    public ResponseBody getTemplateByTypes(@RequestParam(value = "assetTypeId", required = true) String assetTypeId,
                                           @RequestParam(value = "warningType", required = true) String warningType,
                                           @RequestParam(value = "severity", required = true) String severity,
                                           @RequestParam(value = "channel", required = true) String channel) {
        Template template = new Template();
        template.setAssetTypeId(assetTypeId);
        template.setWarningType(warningType);
        template.setSeverity(severity);
        template.setContactChannel(channel);
        return new ResponseBody(Code.SELECT_OK, warningService.getNotificationTemplateByTypes(template));
    }

    @GetMapping("/template/id/{id}")
    public ResponseBody getTemplateById(@PathVariable Long id) {
        return new ResponseBody(Code.SELECT_OK, warningService.getNotificationTemplateById(id));
    }

    @PostMapping("/template")
    public ResponseBody insertTemplate(@RequestBody Template template) {
        template.setId(null);
        return new ResponseBody(Code.INSERT_OK, warningService.insertNotificationTemplate(template));
    }

    @PutMapping("/template/type")
    public ResponseBody updateTemplateByTypes(@RequestBody Template template) {
        return new ResponseBody(Code.UPDATE_OK, warningService.updateNotificationTemplateMessageByTypes(template));
    }

    @PutMapping("/template/id")
    public ResponseBody updateTemplateById(@RequestBody Template template) {
        return new ResponseBody(Code.UPDATE_OK, warningService.updateNotificationTemplateMessageById(template));
    }

    @DeleteMapping("/template")
    public ResponseBody deleteTemplatesByIds(@RequestBody Map<String, Object> body) {
        List<Long> ids = (List<Long>) body.get("ids");
        return new ResponseBody(Code.DELETE_OK, warningService.deleteNotificationTemplateByIds(ids));
    }

    @GetMapping("/permission")
    public ResponseBody getAllPermissions(@RequestParam(required = false) List<String> orderList,
                                          @RequestParam(required = false) Integer limit,
                                          @RequestParam(required = false) Integer offset) {
        return new ResponseBody(Code.SELECT_OK, permissionConfigService.getAllPermissionConfigs(null, QueryTool.getOrderList(orderList), limit, offset));
    }

    @PostMapping("/permission/search")
    public ResponseBody getAllPermissions(@RequestBody FilterDTO filter) {
        if (!filter.hasOrderList() && (filter.hasLimit() || filter.hasOffset())) {
            throw new SpExceptions.BadRequestException("Pagination parameters specified without order list.");
        }

        return new ResponseBody(Code.SELECT_OK, permissionConfigService.getAllPermissionConfigs(
                filter.getFilters(),
                QueryTool.getOrderList(filter.getOrderList()),
                filter.getLimit(),
                filter.getOffset()
        ));
    }

    @GetMapping("/permission/{uid}")
    public ResponseBody getPermissionByUserId(@PathVariable String uid) {
        return new ResponseBody(Code.SELECT_OK, permissionConfigService.getPermissionConfigByUserId(uid));
    }

    @PostMapping("/permission")
    public ResponseBody insertPermissionConfig(@RequestBody PermissionConfig permissionConfig) {
        return new ResponseBody(Code.INSERT_OK, permissionConfigService.insertPermissionConfig(permissionConfig));
    }

    @PutMapping("/permission")
    public ResponseBody updatePermissionConfig(@RequestBody PermissionConfig permissionConfig) {
        return new ResponseBody(Code.UPDATE_OK, permissionConfigService.updatePermissionConfigByUserId(permissionConfig));
    }
}
