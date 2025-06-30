package uk.ac.bristol.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class ScheduledNotifier {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

//    @Scheduled(fixedRate = 10000)
//    public void sendMessage() {
//        messagingTemplate.convertAndSend("/topic/notify", "当前时间：" + LocalDateTime.now());
//    }
}
