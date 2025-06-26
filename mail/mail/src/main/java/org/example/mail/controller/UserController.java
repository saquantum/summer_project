package org.example.mail.controller;

import org.example.mail.dao.UserDiscordMapper;
import org.example.mail.dao.UserEmailMapper;
import org.example.mail.dao.UserWhatsAppMapper;
import org.example.mail.pojo.UserDiscord;
import org.example.mail.pojo.UserEmail;
import org.example.mail.pojo.UserWhatsApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserEmailMapper emailMapper;

    @Autowired
    private UserWhatsAppMapper whatsAppMapper;

    @Autowired
    private UserDiscordMapper discordMapper;

    //email相关
    @PostMapping("/add")
    public ResponseEntity<String> addEmail(@RequestParam String email) {
        String tokenId = UUID.randomUUID().toString();
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("Invalid email!");
        }
        if (emailMapper.existsByEmail(email)) {
            return ResponseEntity.badRequest().body("Email already exists!");
        }

        String emailRegex = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        if(!matcher.matches()) {
            return ResponseEntity.badRequest().body("Invalid email!");
        }

        //邮箱域名检查
        if(!isDomainResolvable(email)) {
            return ResponseEntity.badRequest().body("Invalid email!");
        }

        UserEmail ue = new UserEmail();
        ue.setEmail(email);
        ue.setUid(tokenId);
        emailMapper.insertEmail(ue);
        return ResponseEntity.ok("Email added successfully!");
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

    //Discord相关
    @PostMapping("/add-discord")
    public String addDiscord(@RequestParam String discord) {
        if (discord== null || discord.isBlank()) {
            return "Invalid discord url!";
        }
        if (discordMapper.existsByDiscord(discord)) {
            return "Discord url already exists!";
        }
        UserDiscord userDiscord = new UserDiscord();
        userDiscord.setDiscord(discord);
        discordMapper.insertDiscord(userDiscord);
        return "Discord url added successfully!";
    }

    @GetMapping("/list")
    public List<UserEmail> list() {
        return emailMapper.selectAllEmails();
    }

    public static boolean isDomainResolvable(String email) {
        try {
            String domain = email.substring(email.indexOf("@") + 1);
            InetAddress.getByName(domain); // 尝试解析域名
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}