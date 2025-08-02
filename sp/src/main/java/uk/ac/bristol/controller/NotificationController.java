package uk.ac.bristol.controller;

import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.advice.PostSearchEndpoint;
import uk.ac.bristol.advice.UserIdentificationExecution;
import uk.ac.bristol.advice.UserUID;
import uk.ac.bristol.pojo.FilterDTO;
import uk.ac.bristol.service.ContactService;
import uk.ac.bristol.service.WarningService;
import uk.ac.bristol.util.QueryTool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class NotificationController {

    private final ContactService contactService;
    private final WarningService warningService;

    public NotificationController(ContactService contactService, WarningService warningService) {
        this.contactService = contactService;
        this.warningService = warningService;
    }

    /* ---------------- Inbox ---------------- */

    @UserIdentificationExecution
    @GetMapping("/user/uid/{uid}/inbox")
    public ResponseBody getMyInboxMessagesByUID(HttpServletResponse response,
                                                HttpServletRequest request,
                                                @UserUID @PathVariable String uid) {
        return new ResponseBody(Code.SELECT_OK, contactService.getUserInboxMessagesByUserId(uid));
    }

    @GetMapping("/admin/user/uid/{uid}/inbox")
    public ResponseBody getUserInboxMessagesByUID(HttpServletResponse response,
                                                  HttpServletRequest request,
                                                  @UserUID @PathVariable String uid) {
        return new ResponseBody(Code.SELECT_OK, contactService.getUserInboxMessagesByUserId(uid));
    }

    @UserIdentificationExecution
    @PutMapping("/user/uid/{uid}/inbox/{rowId}")
    public ResponseBody setMyInboxMessageReadByRowIdWithUID(HttpServletResponse response,
                                                            HttpServletRequest request,
                                                            @UserUID @PathVariable String uid,
                                                            @PathVariable long rowId,
                                                            @RequestParam(required = false, value = "has-read") Boolean read) {
        boolean hasRead = read != null ? read : true;
        contactService.updateInboxMessageByUserId(Map.of("rowId", rowId, "userId", uid, "hasRead", hasRead));
        return new ResponseBody(Code.UPDATE_OK, null);
    }

    @UserIdentificationExecution
    @DeleteMapping("/user/uid/{uid}/inbox/{rowId}")
    public ResponseBody deleteMyInboxMessageByRowIdWithUID(HttpServletResponse response,
                                                           HttpServletRequest request,
                                                           @UserUID @PathVariable String uid,
                                                           @PathVariable long rowId) {
        contactService.deleteInboxMessageByFilter(Map.of("inbox_row_id", rowId));
        return new ResponseBody(Code.DELETE_OK, null);
    }

    @PostMapping("/admin/notify/inbox")
    public ResponseBody sendInboxMessageToUser(@RequestBody Map<String, Object> message) {
        String userId = (String) message.get("userId");
        String title = (String) message.get("title");
        String body = (String) message.get("body");
        Long validDuration = Long.valueOf((Integer) message.get("duration"));

        LocalDateTime now = LocalDateTime.now();
        int n = contactService.insertInboxMessageToUser(Map.of(
                "userId", userId,
                "hasRead", false,
                "issuedDate", now,
                "validUntil", now.plus(Duration.ofMinutes(validDuration)),
                "title", title,
                "message", body)
        );
        return new ResponseBody(Code.SUCCESS, n, "Successfully sent " + n + " inbox messages.");
    }

    // TODO: split inbox tables into 2 tables: user-specific messages and common messages visible to all users.
    // A problem: users registered later should receive the same old messages?

    @PostMapping("/admin/notify/inbox/all")
    public ResponseBody sendInboxMessageToUsersByFilter(@RequestBody Map<String, Object> message) {
        Map<String, Object> filter = (Map<String, Object>) message.get("filters");
        String title = (String) message.get("title");
        String body = (String) message.get("body");
        Long validDuration = Long.valueOf((Integer) message.get("duration"));

        LocalDateTime now = LocalDateTime.now();
        int n = contactService.insertInboxMessageToUsersByFilter(
                filter,
                Map.of(
                        "hasRead", false,
                        "issuedDate", now,
                        "validUntil", now.plus(Duration.ofMinutes(validDuration)),
                        "title", title,
                        "message", body)
        );
        return new ResponseBody(Code.SUCCESS, n, "Successfully sent " + n + " inbox messages.");
    }

    /* ---------------- Notifications ---------------- */

    @GetMapping("/user/notify/email/unsubscribe")
    public ResponseBody unsubscribeEmail(@RequestParam(required = true) String token) {
        return contactService.unsubscribeEmail(token);
    }

    @PostMapping("/admin/notify/sms")
    public ResponseBody test__sendTestSms(@RequestParam(value = "to") String toPhoneNumber) {
        contactService.sendSms(toPhoneNumber, "[System Notice] Your verification code is 123456. Please do not share it with others.");
        return new ResponseBody(Code.SUCCESS, null, "The SMS has been sent.");
    }

    /* ---------------- Warnings ---------------- */

    @GetMapping("/warning")
    public ResponseBody getAllLiveWarnings(@RequestParam(required = false) List<String> orderList,
                                           @RequestParam(required = false) Integer limit,
                                           @RequestParam(required = false) Integer offset) {
        FilterDTO filter = new FilterDTO(limit, offset);
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, warningService.getWarnings(
                null,
                QueryTool.getOrderList(orderList),
                filter.getLimit(),
                offset
        ), message);
    }

    @PostSearchEndpoint
    @PostMapping("/warning/search")
    public ResponseBody getAllLiveWarnings(@RequestBody FilterDTO filter) {
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, warningService.getCursoredWarnings(
                filter.getLastRowId(),
                filter.getFilters(),
                QueryTool.getOrderList(filter.getOrderList()),
                filter.getLimit(),
                filter.getOffset()
        ), message);
    }

    @GetMapping("/admin/warning/all")
    public ResponseBody getAllWarningsIncludingOutdated(@RequestParam(required = false) List<String> orderList,
                                                        @RequestParam(required = false) Integer limit,
                                                        @RequestParam(required = false) Integer offset) {
        FilterDTO filter = new FilterDTO(limit, offset);
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, warningService.getWarningsIncludingOutdated(
                null,
                QueryTool.getOrderList(orderList),
                filter.getLimit(),
                offset
        ), message);
    }

    @PostSearchEndpoint
    @PostMapping("/admin/warning/all/search")
    public ResponseBody getAllWarningsIncludingOutdated(@RequestBody FilterDTO filter) {
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, warningService.getCursoredWarningsIncludingOutdated(
                filter.getLastRowId(),
                filter.getFilters(),
                QueryTool.getOrderList(filter.getOrderList()),
                filter.getLimit(),
                filter.getOffset()
        ), message);
    }

    @GetMapping("/warning/{id}")
    public ResponseBody getWarningById(@PathVariable Long id) {
        return new ResponseBody(Code.SELECT_OK, warningService.getWarningById(id));
    }

    // NOTICE: no post or put mapping for warnings, since they should be handled by the crawler.

    @DeleteMapping("/admin/warning")
    public ResponseBody deleteWarningsByIds(@RequestBody Map<String, Object> body) {
        List<Long> ids = (List<Long>) body.get("ids");
        return new ResponseBody(Code.DELETE_OK, warningService.deleteWarningByIDs(ids));
    }
}
