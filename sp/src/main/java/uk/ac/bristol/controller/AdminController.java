package uk.ac.bristol.controller;

import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.advice.PostSearchEndpoint;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.AccessControlGroup;
import uk.ac.bristol.pojo.FilterDTO;
import uk.ac.bristol.pojo.Template;
import uk.ac.bristol.service.*;
import uk.ac.bristol.util.QueryTool;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    private final MetaDataService metaDataService;
    private final AccessControlService accessControlService;
    private final ContactService contactService;
    private final UserService userService;
    private final AssetService assetService;

    public AdminController(MetaDataService metaDataService, AccessControlService accessControlService, ContactService contactService, UserService userService, AssetService assetService) {
        this.metaDataService = metaDataService;
        this.accessControlService = accessControlService;
        this.contactService = contactService;
        this.userService = userService;
        this.assetService = assetService;
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

    @GetMapping("/access-group")
    public ResponseBody getAllAccessControlGroups(@RequestParam(required = false) List<String> orderList,
                                                  @RequestParam(required = false) Integer limit,
                                                  @RequestParam(required = false) Integer offset) {
        FilterDTO filter = new FilterDTO(limit, offset);
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, accessControlService.getAccessControlGroups(
                null,
                QueryTool.getOrderList(orderList),
                filter.getLimit(),
                offset
        ), message);
    }

    @PostSearchEndpoint
    @PostMapping("/access-group/search")
    public ResponseBody getAllAccessControlGroups(@RequestBody FilterDTO filter) {
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, accessControlService.getCursoredAccessControlGroups(
                filter.getLastRowId(),
                filter.getFilters(),
                QueryTool.getOrderList(filter.getOrderList()),
                filter.getLimit(),
                filter.getOffset()
        ), message);
    }

    @GetMapping("/access-group/{uid}")
    public ResponseBody getAccessControlGroupByUserId(@PathVariable String uid) {
        return new ResponseBody(Code.SELECT_OK, accessControlService.getAccessControlGroupByUserId(uid));
    }

    @GetMapping("/access-group/system")
    public ResponseBody getSystemShutdown() {
        return new ResponseBody(Code.SELECT_OK, AccessControlGroup.systemShutdown);
    }

    @PostMapping("/access-group")
    public ResponseBody insertAccessControlGroup(@RequestBody AccessControlGroup.AccessControlGroupInterface group) {
        return new ResponseBody(Code.INSERT_OK, accessControlService.insertAccessControlGroup(new AccessControlGroup(group)));
    }

    @PutMapping("/access-group")
    public ResponseBody updateAccessControlGroup(@RequestBody AccessControlGroup.AccessControlGroupInterface group) {
        return new ResponseBody(Code.UPDATE_OK, accessControlService.updateAccessControlGroup(new AccessControlGroup(group)));
    }

    @PutMapping("/access-group/system")
    public ResponseBody updateSystemShutdown(@RequestBody AccessControlGroup.AccessControlGroupInterface group) {
        if (group == null) {
            throw new SpExceptions.PutMethodException("Invalid system shutdown settings");
        }
        if (group.getName() != null && !group.getName().isBlank() && !"system shutdown".equals(group.getName())) {
            System.out.println(group.getName());
            throw new SpExceptions.PutMethodException("Please remain the name of system shutdown and do not modify it.");
        }
        group.setRowId(-1L);
        group.setName("system shutdown");
        AccessControlGroup.systemShutdown = new AccessControlGroup(new AccessControlGroup(group));
        return new ResponseBody(Code.UPDATE_OK, AccessControlGroup.systemShutdown);
    }

    @PutMapping("/access-group/assign/{groupName}")
    public ResponseBody assignUsersToAccessControlGroupByFilter(@RequestBody FilterDTO filter, @PathVariable String groupName) {
        Map<String, Integer> map = accessControlService.assignUsersToGroupByFilter(groupName, filter.getFilters());
        return new ResponseBody(Code.UPDATE_OK, null, "assigned " + map.get("inserted") + " previously orphaned users, updated " + map.get("updated") + " users");
    }

    /* dashboard */

    @GetMapping("/dashboard/users/country")
    public ResponseBody getCountUsersByCountry() {
        return new ResponseBody(Code.SELECT_OK, userService.groupUserAddressPostcodeByCountry(Map.of("user_is_admin", false)));
    }

    @GetMapping("/dashboard/users/region")
    public ResponseBody getCountUsersByRegion() {
        return new ResponseBody(Code.SELECT_OK, userService.groupUserAddressPostcodeByRegion(Map.of("user_is_admin", false)));
    }

    @GetMapping("/dashboard/users/district")
    public ResponseBody getCountUsersByDistrict() {
        return new ResponseBody(Code.SELECT_OK, userService.groupUserAddressPostcodeByAdminDistrict(Map.of("user_is_admin", false)));
    }

    @GetMapping("/dashboard/users/contact-preference")
    public ResponseBody getUserContactPreferencePercentage() {
        return new ResponseBody(Code.SELECT_OK, userService.getUserContactPreferencesPercentage(Map.of("user_is_admin", false)));
    }

    @GetMapping("/dashboard/assets/region")
    public ResponseBody getCountAssetsByRegion() {
        return new ResponseBody(Code.SELECT_OK, assetService.groupAssetLocationByRegion(null));
    }

}
