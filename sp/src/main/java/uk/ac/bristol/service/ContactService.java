package uk.ac.bristol.service;

import uk.ac.bristol.controller.ResponseBody;
import uk.ac.bristol.pojo.Warning;

import java.util.List;
import java.util.Map;

public interface ContactService {

    Map<String, Object> formatNotificationWithIds(Long warningId, String assetId, String ownerId);

    Map<String, Object> formatNotification(Long warningId, String assetId);

    void sendAllEmails(Warning warning, List<String> assetIds);

    ResponseBody sendEmail(Map<String, Object> notification);

    ResponseBody unsubscribeEmail(String token);

    void sendSms(String toPhoneNumber, String messageBody);

    ResponseBody generateCode(String email);

    ResponseBody validateCode(String email, String code);

    ResponseBody registerGenerateCode(String email);
}
