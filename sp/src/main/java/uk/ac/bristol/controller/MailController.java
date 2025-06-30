package uk.ac.bristol.controller;

import org.apache.ibatis.mapping.ResultMap;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.bristol.dao.AssetHolderMapper;
import uk.ac.bristol.pojo.AssetHolder;
import uk.ac.bristol.util.MailUtil;
import uk.ac.bristol.util.QueryTool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class MailController {

    @Value("${spring.mail.username}")
    private String from;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private AssetHolderMapper assetHolderMapper;

    @PostMapping("/send")
    public String sendEmailToAllSubUsers() {
        String subject = "This is subject";

        List<AssetHolder> list = assetHolderMapper.selectAllAssetHolders(null,null,null);

        for (AssetHolder ah:list){
            String email = ah.getEmail();
            Map contactPreferences= (Map) assetHolderMapper.selectContactPreferencesByAssetHolderId(ah.getId());
            Boolean emailPreference = (Boolean) contactPreferences.get("email");
            if(emailPreference){

                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(from);
                message.setTo(email);
                message.setSubject(subject);

                String token = MailUtil.generateToken(email);
                String unsubscribeUrl = "http://localhost:8080/unsubscribe-email?token=" + token;

                String content = "This is content" + unsubscribeUrl;
                message.setText(content);

                mailSender.send(message);
            }
        }
        return "Success!";
    }

    @GetMapping("/unsubscribe-email")
    @ResponseBody
    public String unsubscribe1(@RequestParam String token) {
        try {
            String email = MailUtil.parseToken(token);
            List<AssetHolder> list = assetHolderMapper.selectAllAssetHolders(null,null,null);
            for(AssetHolder ah:list){
                if(ah.getEmail().equals(email)){
                    Map contactPreferences = ah.getContactPreferences();

                    List<Map<String, Object>> listContactPreferences = assetHolderMapper.selectContactPreferencesByAssetHolderId(ah.getId());
                    if((boolean) listContactPreferences.get(0).get("email")){
                        contactPreferences.put("email", false);
                        assetHolderMapper.updateContactPreferencesByAssetHolderId(contactPreferences);
                        return "Successfully Unsubscribed: " + email;
                    } else {
                        return "Already Unsubscribed: " + email;
                    }
                } else {
                    return "Email not found: " + email;
                }
            }
        } catch (Exception e) {
            return "Unsubscribe link invalid or expired.";
        }
        return "Unsubscribe link invalid or expired.";
    }
}
