package uk.ac.bristol.service;

import uk.ac.bristol.controller.ResponseBody;

import java.util.Map;

public interface ContactService {

    Map<String, Object> formatNotification(Long warningId, String assetId, Integer messageId);

    ResponseBody sendEmail(Map<String, Object> notification);

    ResponseBody unsubscribeEmail(String token);
}
