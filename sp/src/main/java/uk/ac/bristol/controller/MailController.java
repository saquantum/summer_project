package uk.ac.bristol.controller;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.dao.AssetHolderMapper;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.AssetHolder;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.pojo.UserWithAssetHolder;
import uk.ac.bristol.service.UserService;
import uk.ac.bristol.util.JwtUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class MailController {

    @Value("${spring.mail.username}")
    private String from;

    @Value("${app.base-url}")
    private String baseURL;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private AssetHolderMapper assetHolderMapper;
    @Autowired
    private UserService userService;

    @PostMapping("/admin/notify/email")
    public String sendEmailToAllSubUsers() {
        String subject = "This is subject";

        List<AssetHolder> list = assetHolderMapper.selectAllAssetHolders(null, null, null);

        for (AssetHolder ah : list) {

            String email = ah.getEmail();
            Map contactPreferences = (Map) assetHolderMapper.selectContactPreferencesByAssetHolderId(ah.getId());
            Boolean emailPreference = (Boolean) contactPreferences.get("email");
            if (emailPreference) {

                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(from);
                message.setTo(email);
                message.setSubject(subject);

                Map<String, Object> claims = new HashMap<>();
                claims.put("unsubscribe-email-uid", email);
                claims.put("action", "unsubscribe-email");
                String unsubscribeUrl = baseURL + "/user/notify/email/unsubscribe?token=" + JwtUtil.generateJWT(claims);

                String content = "This is content" + unsubscribeUrl;
                message.setText(content);

                mailSender.send(message);
            }
        }
        return "Success!";
    }

    @GetMapping("/user/notify/email/unsubscribe")
    public ResponseBody unsubscribe(@RequestParam(required = true) String token) {
        Claims claims = JwtUtil.parseJWT(token);
        if (!"unsubscribe-email".equals(claims.get("action"))) {
            throw new SpExceptions.BusinessException("The token provided is incorrect to unsubscribe email");
        }
        String uid = claims.get("unsubscribe-email-uid").toString();

        User user = userService.getUserByUserId(uid, null, null, null);
        AssetHolder ah = user.getAssetHolder();
        if (ah == null) {
            throw new SpExceptions.BusinessException("The user has no active asset holder details");
        }
        ah.getContactPreferences().put("email", false);
        return new ResponseBody(Code.DELETE_OK, null, "Successfully unsubscribed email for user " + uid);
    }
}
