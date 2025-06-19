package org.example.mail.controller;

import org.example.mail.dao.UserEmailMapper;
import org.example.mail.pojo.UserEmail;
import org.example.mail.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserEmailMapper emailMapper;

    @PostMapping("/send")
    public String sendToAllUsers(@RequestBody Map<String, String> mailData) {
        String subject = mailData.get("subject");
        String contentTemplate = mailData.get("content");

        if (subject == null || subject.isEmpty() || contentTemplate == null || contentTemplate.isEmpty()) {
            return "Subject or content is empty!";
        }

        List<UserEmail> allEmails = emailMapper.selectAllEmails();

        for (UserEmail user : allEmails) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("alchemistbackydian@gmail.com");
            message.setTo(user.getEmail());
            message.setSubject(subject);

            String token = JwtUtil.generateToken(user.getEmail());
            String unsubscribeUrl = "http://localhost:8080/unsubscribe?token=" + token;

            String content = contentTemplate.replace("{unsubscribeUrl}", unsubscribeUrl);

            message.setText(content);

            mailSender.send(message);
        }

        return "Success, number of emails: " + allEmails.size();
    }
}