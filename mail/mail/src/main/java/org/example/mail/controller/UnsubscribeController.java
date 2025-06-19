package org.example.mail.controller;

import org.example.mail.dao.UserEmailMapper;
import org.example.mail.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class UnsubscribeController {

    @Autowired
    private UserEmailMapper emailMapper;

    @GetMapping("/unsubscribe")
    @ResponseBody
    public String unsubscribe(@RequestParam String token) {
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
}