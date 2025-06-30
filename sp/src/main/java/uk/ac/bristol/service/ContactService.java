package uk.ac.bristol.service;

import java.util.Map;

public interface ContactService {
    Map<String, Object> formatNotification();

    void sendEmail();

    void unsubscribeEmail(String token);
}
