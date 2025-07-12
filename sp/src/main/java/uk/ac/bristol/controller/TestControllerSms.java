package uk.ac.bristol.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.service.SmsService;

@RestController
@RequestMapping("/test")
public class TestControllerSms {

    private final SmsService smsService;

    @Autowired
    public TestControllerSms(SmsService smsService) {
        this.smsService = smsService;
    }

    @GetMapping("/sendTestSms")
    public String sendTestSms(@RequestParam String to) {
        smsService.sendSms(to, "[System Notice] Your verification code is 123456. Please do not share it with others.");
        return "The SMS sending request has been sent, please check your cell phone for SMS.";
    }
}