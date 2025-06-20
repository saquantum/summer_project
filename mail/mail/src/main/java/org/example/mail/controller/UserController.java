package org.example.mail.controller;

import org.example.mail.dao.UserEmailMapper;
import org.example.mail.dao.UserWhatsAppMapper;
import org.example.mail.pojo.UserEmail;
import org.example.mail.pojo.UserWhatsApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserEmailMapper emailMapper;

    @Autowired
    private UserWhatsAppMapper whatsAppMapper;

    //email相关
    @PostMapping("/add")
    public String addEmail(@RequestParam String email) {
        if (email == null || email.isBlank()) {
            return "Invalid email!";
        }
        if (emailMapper.existsByEmail(email)) {
            return "Email already exists!";
        }
        UserEmail ue = new UserEmail();
        ue.setEmail(email);
        emailMapper.insertEmail(ue);
        return "Email added successfully!";
    }

    //whatsapp相关
    @PostMapping("/add-whatsapp")
    public String addWhatsApp(@RequestParam String phoneNumber) {
        if (phoneNumber== null || phoneNumber.isBlank()) {
            return "Invalid whatsapp number!";
        }
        if (whatsAppMapper.existsByWhatsApp(phoneNumber)) {
            return "WhatsApp number already exists!";
        }
        UserWhatsApp userWhatsApp = new UserWhatsApp();
        userWhatsApp.setPhoneNumber(phoneNumber);
        whatsAppMapper.insert(userWhatsApp);
        return "WhatsApp added successfully!";
    }

    @GetMapping("/list")
    public List<UserEmail> list() {
        return emailMapper.selectAllEmails();
    }
}