package uk.ac.bristol.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.bristol.advice.PostSearchEndpoint;
import uk.ac.bristol.pojo.FilterDTO;
import uk.ac.bristol.pojo.PermissionConfig;
import uk.ac.bristol.pojo.Template;
import uk.ac.bristol.service.*;
import uk.ac.bristol.util.QueryTool;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    private final MetaDataService metaDataService;
    private final PermissionConfigService permissionConfigService;
    private final ContactService contactService;
    private final PostcodeService postcodeService;

    public AdminController(MetaDataService metaDataService, PermissionConfigService permissionConfigService, ContactService contactService, PostcodeService postcodeService, PermissionGroupService permissionGroupService) {
        this.metaDataService = metaDataService;
        this.permissionConfigService = permissionConfigService;
        this.contactService = contactService;
        this.postcodeService = postcodeService;
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
        FilterDTO filter = new FilterDTO(limit, offset);
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, contactService.getNotificationTemplates(
                null,
                QueryTool.getOrderList(orderList),
                filter.getLimit(),
                offset
        ), message);
    }

    @PostSearchEndpoint
    @PostMapping("/template/search")
    public ResponseBody getAllTemplates(@RequestBody FilterDTO filter) {
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, contactService.getCursoredNotificationTemplates(
                filter.getLastRowId(),
                filter.getFilters(),
                QueryTool.getOrderList(filter.getOrderList()),
                filter.getLimit(),
                filter.getOffset()
        ), message);
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
        return new ResponseBody(Code.SELECT_OK, contactService.getNotificationTemplateByTypes(template));
    }

    @GetMapping("/template/id/{id}")
    public ResponseBody getTemplateById(@PathVariable Long id) {
        return new ResponseBody(Code.SELECT_OK, contactService.getNotificationTemplateById(id));
    }

    @PostMapping("/template")
    public ResponseBody insertTemplate(@RequestBody Template template) {
        template.setId(null);
        return new ResponseBody(Code.INSERT_OK, contactService.insertNotificationTemplate(template));
    }

    @PutMapping("/template/type")
    public ResponseBody updateTemplateByTypes(@RequestBody Template template) {
        return new ResponseBody(Code.UPDATE_OK, contactService.updateNotificationTemplateMessageByTypes(template));
    }

    @PutMapping("/template/id")
    public ResponseBody updateTemplateById(@RequestBody Template template) {
        return new ResponseBody(Code.UPDATE_OK, contactService.updateNotificationTemplateMessageById(template));
    }

    @DeleteMapping("/template")
    public ResponseBody deleteTemplatesByIds(@RequestBody Map<String, Object> body) {
        List<Long> ids = (List<Long>) body.get("ids");
        return new ResponseBody(Code.DELETE_OK, contactService.deleteNotificationTemplateByIds(ids));
    }

    @GetMapping("/permission")
    public ResponseBody getAllPermissions(@RequestParam(required = false) List<String> orderList,
                                          @RequestParam(required = false) Integer limit,
                                          @RequestParam(required = false) Integer offset) {
        FilterDTO filter = new FilterDTO(limit, offset);
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, permissionConfigService.getPermissionConfigs(
                null,
                QueryTool.getOrderList(orderList),
                filter.getLimit(),
                offset
        ), message);
    }

    @PostSearchEndpoint
    @PostMapping("/permission/search")
    public ResponseBody getAllPermissions(@RequestBody FilterDTO filter) {
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, permissionConfigService.getCursoredPermissionConfigs(
                filter.getLastRowId(),
                filter.getFilters(),
                QueryTool.getOrderList(filter.getOrderList()),
                filter.getLimit(),
                filter.getOffset()
        ), message);
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

    @GetMapping("/dashboard/users/country")
    public ResponseBody getCountUsersByCountry() {
        return new ResponseBody(Code.SELECT_OK,
                postcodeService.groupUserAddressPostcodeByCountry(Map.of("user_is_admin", false))
                        .entrySet()
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        Map.Entry::getKey,
                                        e -> e.getValue().size()
                                )
                        )
        );
    }

    @GetMapping("/dashboard/users/region")
    public ResponseBody getCountUsersByRegion() {
        return new ResponseBody(Code.SELECT_OK,
                postcodeService.groupUserAddressPostcodeByRegion(Map.of("user_is_admin", false))
                        .entrySet()
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        Map.Entry::getKey,
                                        e -> e.getValue().size()
                                )
                        )
        );
    }

    @GetMapping("/dashboard/users/district")
    public ResponseBody getCountUsersByDistrict() {
        return new ResponseBody(Code.SELECT_OK,
                postcodeService.groupUserAddressPostcodeByAdminDistrict(Map.of("user_is_admin", false))
                        .entrySet()
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        Map.Entry::getKey,
                                        e -> e.getValue().size()
                                )
                        )
        );
    }
}
