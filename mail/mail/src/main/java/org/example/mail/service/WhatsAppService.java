package org.example.mail.service;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import org.example.mail.config.TwilioConfig;
import org.springframework.stereotype.Service;
import com.twilio.type.PhoneNumber;

@Service
public class WhatsAppService {

    private final TwilioConfig twilioConfig;

    public WhatsAppService(TwilioConfig twilioConfig) {
        this.twilioConfig = twilioConfig;
        Twilio.init(twilioConfig.getAccountSid(), twilioConfig.getAuthToken());
    }

    public void sendMessage(String toPhoneNumber, String message) {
        try {
            System.out.println("Sending WhatsApp message to: whatsapp:" + toPhoneNumber);
            System.out.println("From WhatsApp number: " + twilioConfig.getWhatsappFrom());
            Message.creator(
                    new PhoneNumber("whatsapp:" + toPhoneNumber),
                    new PhoneNumber(twilioConfig.getWhatsappFrom()),
                    message
            ).create();
        } catch (ApiException e) {
            System.err.println("Twilio API Error: " + e.getMessage());
            throw e;
        }
    }
}
