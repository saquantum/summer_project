package uk.ac.bristol.service;

public interface SmsService {
    void sendSms(String to, String messageBody);
}
