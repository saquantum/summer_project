package org.example.mail.controller;

import org.example.mail.dao.UserEmailMapper;
import org.example.mail.dao.UserWhatsAppMapper;
import org.example.mail.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class UnsubscribeController {

    @Autowired
    private UserEmailMapper emailMapper;

    @Autowired
    private UserWhatsAppMapper whatsAppMapper;

    @GetMapping("/unsubscribe-email")
    @ResponseBody
    public String unsubscribe1(@RequestParam String token) {
        try {
            String email = JwtUtil.parseToken(token);

            int deleted = emailMapper.deleteByEmail(email);
            if (deleted > 0) {
                return "Successfully Unsubscribed: " + email;
            } else {
                return "Email not found or already unsubscribed: " + email;
            }
        } catch (Exception e) {
            return "Unsubscribe link invalid or expired.";
        }
    }

    @GetMapping("/unsubscribe-whatsapp")
    @ResponseBody
    public String unsubscribe2(@RequestParam String token) {
        try {
            String phoneNumber = JwtUtil.parseToken(token);

            int deleted = whatsAppMapper.deleteByWhatsApp(phoneNumber);
            if (deleted > 0) {
                return "Successfully Unsubscribed: " + phoneNumber;
            } else {
                return "Email not found or already unsubscribed: " + phoneNumber;
            }
        } catch (Exception e) {
            return "Unsubscribe link invalid or expired.";
        }
    }
}