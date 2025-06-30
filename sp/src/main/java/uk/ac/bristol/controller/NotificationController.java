package uk.ac.bristol.controller;

import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.service.ContactService;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class NotificationController {

    private final ContactService contactService;

    public NotificationController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping("/admin/notify/email")
    public ResponseBody sendEmail(@RequestParam(required = true) Long warningId,
                                  @RequestParam(required = true) String assetId,
                                  @RequestParam(required = true) Integer messageId) {
        Map<String, Object> notification = contactService.formatNotification(warningId, assetId, messageId);
        if (notification == null) {
            return new ResponseBody(Code.SUCCESS, null, "Notification is null, skipped sending email");
        }
        return contactService.sendEmail(notification);
    }

    @GetMapping("/user/notify/email/unsubscribe")
    public ResponseBody unsubscribe(@RequestParam(required = true) String token) {
        return contactService.unsubscribeEmail(token);
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
