package uk.ac.bristol.controller;

import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.advice.UserAID;
import uk.ac.bristol.advice.UserUID;
import uk.ac.bristol.exception.SpExceptions;
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

    @GetMapping("/user/uid/{uid}/inbox")
    public ResponseBody userGetMyInboxMessagesByUID(HttpServletResponse response,
                                                    HttpServletRequest request,
                                                    @UserUID @PathVariable String uid) {
        return new ResponseBody(Code.SELECT_OK, contactService.getUserInboxMessagesByUserId(uid));
    }

    @GetMapping("/user/aid/{aid}/inbox")
    public ResponseBody userGetMyInboxMessagesByAID(HttpServletResponse response,
                                                    HttpServletRequest request,
                                                    @UserAID @PathVariable String aid) {
        return new ResponseBody(Code.SELECT_OK, contactService.getUserInboxMessagesByUserId(aid));
    }

    @PutMapping("/user/uid/{uid}/inbox/{rowId}")
    public ResponseBody userSetInboxMessageReadByRowIdWithUID(HttpServletResponse response,
                                                              HttpServletRequest request,
                                                              @UserUID @PathVariable String uid,
                                                              @PathVariable long rowId) {
        contactService.updateInboxMessageByUserId(Map.of("rowId", rowId, "hasRead", true));
        return new ResponseBody(Code.UPDATE_OK, null);
    }

    @PutMapping("/user/aid/{aid}/inbox/{rowId}")
    public ResponseBody userSetInboxMessageReadByRowIdWithAID(HttpServletResponse response,
                                                              HttpServletRequest request,
                                                              @UserAID @PathVariable String aid,
                                                              @PathVariable long rowId) {
        contactService.updateInboxMessageByUserId(Map.of("rowId", rowId, "hasRead", true));
        return new ResponseBody(Code.UPDATE_OK, null);
    }

    @PostMapping("/admin/notify/inbox")
    public ResponseBody sendInboxMessageToUser(@RequestBody Map<String, Object> message) {
        String userId = (String) message.get("userId");
        String title = (String) message.get("title");
        String body = (String) message.get("body");
        Long validDuration = (Long) message.get("duration");

        LocalDateTime now = LocalDateTime.now();
        int n = contactService.insertInboxMessageToUser(Map.of(
                "userId", userId,
                "hasRead", false,
                "issuedDate", now,
                "validUntil", now.plus(Duration.ofMillis(validDuration)),
                "title", title,
                "message", body)
        );
        return new ResponseBody(Code.SUCCESS, n, "Successfully sent " + n + " inbox messages.");
    }

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
        FilterDTO filter = new FilterDTO(limit);
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, warningService.getAllWarnings(
                null,
                QueryTool.getOrderList(orderList),
                filter.getLimit(),
                offset
        ), message);
    }

    @PostMapping("/warning/search")
    public ResponseBody getAllLiveWarnings(@RequestBody FilterDTO filter) {
        if (!filter.hasOrderList() && (filter.hasLimit() || filter.hasOffset())) {
            throw new SpExceptions.BadRequestException("Pagination parameters specified without order list.");
        }
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, warningService.getAllWarnings(
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
        FilterDTO filter = new FilterDTO(limit);
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, warningService.getAllWarningsIncludingOutdated(
                null,
                QueryTool.getOrderList(orderList),
                filter.getLimit(),
                offset
        ), message);
    }

    @PostMapping("/admin/warning/all/search")
    public ResponseBody getAllWarningsIncludingOutdated(@RequestBody FilterDTO filter) {
        if (!filter.hasOrderList() && (filter.hasLimit() || filter.hasOffset())) {
            throw new SpExceptions.BadRequestException("Pagination parameters specified without order list.");
        }
        String message = QueryTool.formatPaginationLimit(filter);
        return new ResponseBody(Code.SELECT_OK, warningService.getAllWarningsIncludingOutdated(
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
