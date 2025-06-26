package org.example.mail.controller;

import org.example.mail.Result.ParsedTokenResult;
import org.example.mail.dao.UserDiscordMapper;
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

    @Autowired
    private UserDiscordMapper discordMapper;

    @GetMapping("/unsubscribe-email")
    @ResponseBody
    public String unsubscribe1(@RequestParam String token) {
        try {
            ParsedTokenResult result = JwtUtil.parseToken(token);

            String email = result.getEmail();
            String uid = result.getTokenId();

            if (!emailMapper.existsByUid(uid)) {
                return "Wrong uid!";
            }

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
            String phoneNumber = JwtUtil.parseTokenWhatsApp(token);

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

    @GetMapping("/unsubscribe-discord")
    @ResponseBody
    public String unsubscribe3(@RequestParam String token) {
        try {
            String url = JwtUtil.parseTokenDiscord(token);

            int deleted = discordMapper.deleteByDiscord(url);
            if (deleted > 0) {
                return "Successfully Unsubscribed: " + url;
            } else {
                return "Url not found or already unsubscribed: " + url;
            }
        } catch (Exception e) {
            return "Unsubscribe link invalid or expired.";
        }
    }
}