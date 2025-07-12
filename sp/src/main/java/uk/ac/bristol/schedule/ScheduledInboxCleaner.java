package uk.ac.bristol.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.ac.bristol.MockDataInitializer;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.service.WarningService;

@Component
public class ScheduledInboxCleaner {

    @Autowired
    WarningService warningService;

    @Scheduled(fixedRate = 86400000)
    public void cleanOutdatedInboxMessages() {
        try {
            MockDataInitializer.latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new SpExceptions.SystemException("InterruptedException threw, failed to start the scheduled crawler");
        }
        int n = warningService.deleteOutDatedInboxMessages();
        System.out.println(n + " inbox messages deleted.");
    }
}
