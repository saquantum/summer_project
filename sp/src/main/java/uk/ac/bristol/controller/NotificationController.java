package uk.ac.bristol.controller;

import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.service.ContactService;
import uk.ac.bristol.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class NotificationController {

    private final ContactService contactService;
    private final UserService userService;

    public NotificationController(ContactService contactService, UserService userService) {
        this.contactService = contactService;
        this.userService = userService;
    }

    @PostMapping("/admin/notify/email")
    public ResponseBody sendEmail(@RequestParam(required = true) Long warningId,
                                  @RequestParam(required = true) String assetId) {
        Map<String, Object> notification = contactService.formatNotification(warningId, assetId);
        if (notification == null) {
            return new ResponseBody(Code.SUCCESS, null, "Notification is null, skipped sending email");
        }
        return contactService.sendEmail(notification);
    }

    @GetMapping("/user/notify/email/unsubscribe")
    public ResponseBody unsubscribe(@RequestParam(required = true) String token) {
        return contactService.unsubscribeEmail(token);
    }

    @PostMapping("/admin/notify/email/test")
    public ResponseBody test__sendEmailToGivenUser(@RequestParam(required = true) String userId,
                                                   @RequestParam(required = true) Long warningId,
                                                   @RequestParam(required = true) String assetId) {
        User user = userService.getUserByUserId(userId);
        if(user == null || user.getAssetHolderId() == null || user.getAssetHolderId().isBlank()){
            return new ResponseBody(Code.BUSINESS_ERR, null, "The user is null or assetHolderId is null");
        }
        return contactService.sendEmail(contactService.formatNotificationWithIds(warningId, assetId, user.getAssetHolderId()));
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
}
