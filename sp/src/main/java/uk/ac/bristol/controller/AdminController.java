package uk.ac.bristol.controller;

import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.FilterDTO;
import uk.ac.bristol.pojo.PermissionConfig;
import uk.ac.bristol.pojo.Template;
import uk.ac.bristol.service.MetaDataService;
import uk.ac.bristol.service.PermissionConfigService;
import uk.ac.bristol.service.WarningService;
import uk.ac.bristol.util.QueryTool;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    private final WarningService warningService;
    private final MetaDataService metaDataService;
    private final PermissionConfigService permissionConfigService;

    public AdminController(WarningService warningService, MetaDataService metaDataService, PermissionConfigService permissionConfigService) {
        this.warningService = warningService;
        this.metaDataService = metaDataService;
        this.permissionConfigService = permissionConfigService;
    }

    @GetMapping("/metadata")
    public ResponseBody getAllMetaData() {
        return new ResponseBody(Code.SELECT_OK, metaDataService.getAllMetaData());
    }

    @GetMapping("/metadata/{tableName}")
    public ResponseBody getMetaDataByTableName(@PathVariable String tableName) {
        return new ResponseBody(Code.SELECT_OK, metaDataService.getMetaDataByTableName(tableName));
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
