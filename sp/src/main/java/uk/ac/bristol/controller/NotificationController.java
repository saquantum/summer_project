package uk.ac.bristol.controller;

import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.advice.UserAID;
import uk.ac.bristol.advice.UserUID;
import uk.ac.bristol.service.ContactService;
import uk.ac.bristol.service.UserService;
import uk.ac.bristol.service.WarningService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class NotificationController {

    private final ContactService contactService;
    private final UserService userService;
    private final WarningService warningService;

    public NotificationController(ContactService contactService, UserService userService, WarningService warningService) {
        this.contactService = contactService;
        this.userService = userService;
        this.warningService = warningService;
    }

    @GetMapping("/user/uid/{uid}/inbox")
    public ResponseBody userGetMyInboxMessagesByUID(HttpServletResponse response,
                                                    HttpServletRequest request,
                                                    @UserUID @PathVariable String uid) {
        return new ResponseBody(Code.SELECT_OK, warningService.getUserInboxMessagesByUserId(uid));
    }

    @GetMapping("/user/uid/{aid}/inbox")
    public ResponseBody userGetMyInboxMessagesByAID(HttpServletResponse response,
                                                    HttpServletRequest request,
                                                    @UserAID @PathVariable String aid) {
        return new ResponseBody(Code.SELECT_OK, warningService.getUserInboxMessagesByUserId(aid));
    }

    @PostMapping("/admin/notify/inbox")
    public ResponseBody sendInboxMessageToUser(@RequestBody Map<String, Object> message) {
        String userId = (String) message.get("userId");
        String title = (String) message.get("title");
        String body = (String) message.get("body");
        Long validDuration = (Long) message.get("duration");

        LocalDateTime now = LocalDateTime.now();
        int n = warningService.insertInboxMessageToUser(Map.of(
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
}
