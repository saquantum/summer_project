package org.example.mail.controller;

import org.example.mail.dao.UserEmailMapper;
import org.example.mail.pojo.UserEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserEmailMapper emailMapper;

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

    @GetMapping("/list")
    public List<UserEmail> list() {
        return emailMapper.selectAllEmails();
    }
}