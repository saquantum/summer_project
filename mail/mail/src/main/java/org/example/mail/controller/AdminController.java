package org.example.mail.controller;

import org.example.mail.dao.UserEmailMapper;
import org.example.mail.dao.UserWhatsAppMapper;
import org.example.mail.pojo.UserEmail;
import org.example.mail.pojo.UserWhatsApp;
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

    @Autowired
    private UserWhatsAppMapper whatsAppMapper;

    @Autowired
    private WhatsAppController whatsAppController;

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

            String token = JwtUtil.generateToken(user.getEmail(),user.getUid());
            String unsubscribeUrl = "http://localhost:8080/unsubscribe-email?token=" + token;

            String content = contentTemplate.replace("{unsubscribeUrl}", unsubscribeUrl);

            message.setText(content);

            mailSender.send(message);
        }

        return "Success, number of emails: " + allEmails.size();
    }

    //whatsapp
    @PostMapping("/send-whatsapp")
    public String sendWhatsAppToAll(@RequestBody Map<String, String> mailData) {
        List<UserWhatsApp> all = whatsAppMapper.selectAll();

        String contentTemplate = mailData.get("content");

        if (contentTemplate == null || contentTemplate.isEmpty()) {
            return "Content is empty!";
        }

        for (UserWhatsApp user : all) {
            String number = user.getPhoneNumber();

            // 模拟发送（实际中可替换为调用 WhatsApp API）

            String token = JwtUtil.createToken(user.getPhoneNumber());
            String unsubscribeUrl = "http://localhost:8080/unsubscribe-whatsapp?token=" + token;

            System.out.println("Sending WhatsApp message to ：" + number);
            whatsAppController.sendMessage(number,contentTemplate + unsubscribeUrl);
        }

        return "WhatsApp is sent to " + all.size() + " account.";
    }
}