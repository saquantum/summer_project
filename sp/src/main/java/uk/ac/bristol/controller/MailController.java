package uk.ac.bristol.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

//这部分用于发送邮件的简单测试
@RestController
@RequestMapping("/api/mail")
public class MailController {

    @Autowired
    private JavaMailSender mailSender;

    @PostMapping("/send")
    public String sendMail(@RequestParam String to,
                           @RequestParam String subject,
                           @RequestParam String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            //填入自己的邮箱
            message.setFrom("alchemistbackydian@gmail.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            return "Email sent successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "email sent unsuccessfully：" + e.getMessage();
        }
    }
}
