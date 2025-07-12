package uk.ac.bristol.service.impl;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.ac.bristol.service.SmsService;

@Service
public class SmsServiceImpl implements SmsService {

    private final String accountSid;
    private final String authToken;
    private final String fromNumber;

    public SmsServiceImpl(@Value("${twilio.account-sid}") String accountSid,
                          @Value("${twilio.auth-token}") String authToken,
                          @Value("${twilio.from-number}") String fromNumber) {
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.fromNumber = fromNumber;
    }

    @Override
    public void sendSms(String to, String messageBody) {
        Twilio.init(accountSid, authToken);
        Message message = Message.creator(
                new com.twilio.type.PhoneNumber(to),
                new com.twilio.type.PhoneNumber(fromNumber),
                messageBody
        ).create();
        System.out.println("Message has been sent，SID: " + message.getSid());
    }
}