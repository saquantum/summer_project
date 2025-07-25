package uk.ac.bristol.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.FilterDTO;
import uk.ac.bristol.pojo.PermissionConfig;
import uk.ac.bristol.pojo.Template;
import uk.ac.bristol.service.ContactService;
import uk.ac.bristol.service.ImageStorageService;
import uk.ac.bristol.service.MetaDataService;
import uk.ac.bristol.service.PermissionConfigService;
import uk.ac.bristol.util.QueryTool;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {

    private final MetaDataService metaDataService;
    private final PermissionConfigService permissionConfigService;
    private final ContactService contactService;
    private final ImageStorageService imageStorageService;

    public AdminController(MetaDataService metaDataService, PermissionConfigService permissionConfigService, ContactService contactService, ImageStorageService imageStorageService) {
        this.metaDataService = metaDataService;
        this.permissionConfigService = permissionConfigService;
        this.contactService = contactService;
        this.imageStorageService = imageStorageService;
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
        FilterDTO filter = new FilterDTO(limit);
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, contactService.getAllNotificationTemplates(
                null,
                QueryTool.getOrderList(orderList),
                filter.getLimit(),
                offset
        ), message);
    }

    @PostMapping("/template/search")
    public ResponseBody getAllTemplates(@RequestBody FilterDTO filter) {
        if (!filter.hasOrderList() && (filter.hasLimit() || filter.hasOffset())) {
            throw new SpExceptions.BadRequestException("Pagination parameters specified without order list.");
        }
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, contactService.getAllNotificationTemplates(
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
        FilterDTO filter = new FilterDTO(limit);
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, permissionConfigService.getPermissionConfigs(
                null,
                QueryTool.getOrderList(orderList),
                filter.getLimit(),
                offset
        ), message);
    }

    @PostMapping("/permission/search")
    public ResponseBody getAllPermissions(@RequestBody FilterDTO filter) {
        if (!filter.hasOrderList() && (filter.hasLimit() || filter.hasOffset())) {
            throw new SpExceptions.BadRequestException("Pagination parameters specified without order list.");
        }
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, permissionConfigService.getPermissionConfigs(
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

    @PostMapping("/image")
    public ResponseBody uploadImage(@RequestParam("cid") String cid, @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseBody(Code.SUCCESS, null, "No image insert to database!");
        }

        try {
//            byte[] bytes = file.getBytes();
//            String base64Image = Base64.getEncoder().encodeToString(bytes);
            String contentType = file.getContentType(); // 例：image/png
            String base64Image = "data:" + contentType + ";base64," +
                    Base64.getEncoder().encodeToString(file.getBytes());

            imageStorageService.insertImage(cid, base64Image);

            return new ResponseBody(Code.SUCCESS, null, "Image insert to database!");
        } catch (IOException e) {
            return new ResponseBody(Code.SYSTEM_ERR, null, "Upload image error!");
        }
    }

    @DeleteMapping("/image")
    public ResponseBody deleteImage(@RequestParam("cid") String cid) {
        try {
            imageStorageService.deleteImage(cid);
            return new ResponseBody(Code.SUCCESS, null, "delete image success!");
        } catch (Exception e) {
            return new ResponseBody(Code.SYSTEM_ERR, null, "delete failed: " + e.getMessage());
        }
    }
}
