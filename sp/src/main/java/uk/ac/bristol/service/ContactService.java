package uk.ac.bristol.service;

import uk.ac.bristol.controller.ResponseBody;

import java.util.Map;

public interface ContactService {

    Map<String, Object> formatNotificationWithIds(Long warningId, String assetId, String ownerId);

    Map<String, Object> formatNotification(Long warningId, String assetId);

    ResponseBody sendEmail(Map<String, Object> notification);

    ResponseBody unsubscribeEmail(String token);

//    ResponseBody sendDiscordToAddress(String whatsappMessage, String url);

    ResponseBody generateCode(String email);

    ResponseBody validateCode(String email, String code);
}
