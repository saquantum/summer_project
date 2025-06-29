package uk.ac.bristol.controller;

import uk.ac.bristol.result.ParsedTokenResult;
import uk.ac.bristol.dao.UserEmailMapper;
import uk.ac.bristol.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UnsubscribeController {

    @Autowired
    private UserEmailMapper emailMapper;

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
}
