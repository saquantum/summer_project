package uk.ac.bristol.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Instant Messaging: Client receives a message only when it's online.
 * To improve: Use Message Queue (Kafka / RabbitMQ / RocketMQ) with database (Redis)
 * <br>
 * <a href="https://docs.spring.io/spring-framework/reference/web/websocket/stomp.html">Spring Stomp</a>
 */
@RestController
@RequestMapping("/admin")
@CrossOrigin
public class BroadcastController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/notify")
    public ResponseEntity<?> sendNotification(
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-Admin-Secret", required = false) String secret) {

        if (!"abc123".equals(secret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Wrong admin authentication");
        }

        String m = body.get("message");
        messagingTemplate.convertAndSend("/topic/notify", m);
        return ResponseEntity.ok("Notification sent");
    }
}
