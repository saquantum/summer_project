package org.example.mail;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

// 学习、测试用的
@SpringBootTest
class MailApplicationTests {

    @Autowired
    JavaMailSenderImpl javaMailSender;

    @Test
    void contextLoads() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("alchemistbackydian@gmail.com");
        message.setTo("1971026049@qq.com");
        message.setSubject("Subject");
        message.setText("Mail sending test!");
        javaMailSender.send(message);
    }
}
