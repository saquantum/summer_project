package uk.ac.bristol.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ScheduledNotifier {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

//    @Scheduled(fixedRate = 10000)
//    public void sendMessage() {
//        messagingTemplate.convertAndSend("/topic/notify", "当前时间：" + LocalDateTime.now());
//    }
}
