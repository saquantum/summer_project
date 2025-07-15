package uk.ac.bristol.service.impl;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.controller.Code;
import uk.ac.bristol.controller.ResponseBody;
import uk.ac.bristol.dao.AssetHolderMapper;
import uk.ac.bristol.dao.AssetMapper;
import uk.ac.bristol.dao.ContactMapper;
import uk.ac.bristol.dao.WarningMapper;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.*;
import uk.ac.bristol.service.ContactService;
import uk.ac.bristol.service.UserService;
import uk.ac.bristol.util.JwtUtil;
import uk.ac.bristol.util.QueryTool;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
@Service
public class ContactServiceImpl implements ContactService {
    @Value("${spring.mail.username}")
    private String from;

    @Value("${app.base-url}")
    private String baseURL;

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private final UserService userService;
    private final WarningMapper warningMapper;
    private final AssetMapper assetMapper;
    private final AssetHolderMapper assetHolderMapper;
    private final ContactMapper contactMapper;


    private final Duration expireTime = Duration.ofMinutes(5);
    private final String prefix = "email:verify:code:";

    public ContactServiceImpl(UserService userService, WarningMapper warningMapper, AssetMapper assetMapper, AssetHolderMapper assetHolderMapper, ContactMapper contactMapper) {
        this.userService = userService;
        this.warningMapper = warningMapper;
        this.assetMapper = assetMapper;
        this.assetHolderMapper = assetHolderMapper;
        this.contactMapper = contactMapper;
    }

    @Override
    public Map<String, Object> formatNotificationWithIds(Long warningId, String assetId, String ownerId) {
        Boolean test = warningMapper.testIfGivenAssetIntersectsWithWarning(assetId, warningId);
        if (test == null || !test) {
            return null;
        }

        List<Warning> warning = warningMapper.selectWarningById(warningId);
        if (warning.size() != 1) {
            throw new SpExceptions.GetMethodException("Get " + warning.size() + " warnings using id " + warningId);
        }

        String typeId = assetMapper.selectAssets(
                QueryTool.formatFilters(Map.of("asset_id", assetId)),
                null, null, null).get(0).getTypeId();
        String warningType = warning.get(0).getWeatherType();
        String severity = warning.get(0).getWarningLevel();

        List<Template> template = contactMapper.selectNotificationTemplateByTypes(new Template(typeId, warningType, severity, "Email"));
        if (template.size() != 1) {
            throw new SpExceptions.SystemException("Get " + template.size() + " templates using id " + warningId);
        }

        String title = template.get(0).getTitle();
        String body = template.get(0).getBody();

        title = fillInVariablesWithValues(title, assetId);
        body = fillInVariablesWithValues(body, assetId);

        if (body == null) {
            throw new SpExceptions.GetMethodException("The message type you required does not exist");
        }

        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        return new HashMap<>(Map.of(
                "id", id,
                "assetTypeId", typeId,
                "toOwnerId", ownerId,
                "weatherWarningType", warningType,
                "severity", severity,
                "title", title,
                "body", body,
                "createdAt", now,
                "validUntil", warning.get(0).getValidTo(),
                "channel", ""));
    }

    @Override
    public Map<String, Object> formatNotification(Long warningId, String assetId) {
        String assetOwnerId = assetMapper.selectAssets(
                QueryTool.formatFilters(Map.of("asset_id", assetId)),
                null, null, null).get(0).getOwnerId();
        return formatNotificationWithIds(warningId, assetId, assetOwnerId);
    }

    @Override
    public void sendAllEmails(Warning warning, List<String> assetIds) {
        for (String assetId : assetIds) {
            Map<String, Object> notification = formatNotification(warning.getId(), assetId);
            sendEmail(notification);
            System.out.println("Successfully sent weather warning notification to " + assetId);
        }
    }

    @Override
    public ResponseBody sendEmail(Map<String, Object> notification) {
        if (notification == null) {
            return new ResponseBody(Code.BUSINESS_ERR, null, "Failed to send email because notification is null");
        }
        List<Map<String, Object>> contactPreferences = assetHolderMapper.selectContactPreferencesByAssetHolderId(notification.get("toOwnerId").toString());
        if (contactPreferences.size() != 1) {
            throw new SpExceptions.SystemException("The database might be modified by another transaction");
        }

        if ((Boolean) contactPreferences.get(0).get("email") == false) {
            return new ResponseBody(Code.SUCCESS, null, "The asset holder prefers not to receive by email");
        }

        List<AssetHolder> holder = assetHolderMapper.selectAssetHolders(
                QueryTool.formatFilters(Map.of("asset_holder_id", notification.get("toOwnerId").toString())),
                null, null, null);
        if (holder.size() != 1) {
            throw new SpExceptions.SystemException("The database might be modified by another transaction");
        }

        String emailAddress = holder.get(0).getEmail();
        sendEmailToAddress(emailAddress, (String) notification.get("title"), (String) notification.get("body"));


        // tmp test
        contactMapper.insertInboxMessageToUser(Map.of(
                "userId", userService.getUserByAssetHolderId(notification.get("toOwnerId").toString()).getId(),
                "hasRead", false,
                "issuedDate", notification.get("createdAt"),
                "validUntil", notification.get("validUntil"),
                "title", notification.get("title"),
                "message", notification.get("body")));


        return new ResponseBody(Code.SUCCESS, notification.get("body"), "The email has been sent to " + emailAddress);
    }

    private void sendEmailToAddress(String toEmailAddress, String emailSubject, String emailContent) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper mmh;
        try {
            mmh = new MimeMessageHelper(message, true, "UTF-8");
            mmh.setFrom(from);
            mmh.setTo(toEmailAddress);
            mmh.setSubject(emailSubject);

            Map<String, Object> claims = new HashMap<>();
            claims.put("unsubscribe-email-uid", toEmailAddress);
            claims.put("action", "unsubscribe-email");
            String unsubscribeUrl = baseURL + "/user/notify/email/unsubscribe?token=" + JwtUtil.generateJWT(claims);
            mmh.setText("<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "    <title>Document</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    emailContent
                    + "<br>"
                    + "<br>"
                    + "To unsubscribe email notifications, click the link below:"
                    + "<br>"
                    + "<a href=\"" + unsubscribeUrl + "\">unsubscribe</a>"
                    + "</body>\n" +
                    "</html>", true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new SpExceptions.SystemException("Failed to send email using mime message helper");
        }
    }

    @Override
    public ResponseBody unsubscribeEmail(String token) {
        Claims claims = JwtUtil.parseJWT(token);
        if (!"unsubscribe-email".equals(claims.get("action"))) {
            throw new SpExceptions.ForbiddenException("The token provided is incorrect to unsubscribe email");
        }
        String uid = claims.get("unsubscribe-email-uid").toString();

        User user = userService.getUserByUserId(uid);
        AssetHolder ah = user.getAssetHolder();
        if (ah == null) {
            throw new SpExceptions.BadRequestException("The user has no active asset holder details");
        }

        ah.getContactPreferences().put("email", false);
        return new ResponseBody(Code.DELETE_OK, null, "Successfully unsubscribed email for user " + uid);
    }

    @Value("${twilio.account-sid}")
    private String twilioAccountSid;
    @Value("${twilio.auth-token}")
    private String twilioAuthToken;
    @Value("${twilio.from-number}")
    private String fromPhoneNumber;

    @Override
    public void sendSms(String toPhoneNumber, String messageBody) {
        Twilio.init(twilioAccountSid, twilioAuthToken);
        Message message = Message.creator(
                new com.twilio.type.PhoneNumber(toPhoneNumber),
                new com.twilio.type.PhoneNumber(fromPhoneNumber),
                messageBody
        ).create();
        System.out.println("Message has been sent，SID: " + message.getSid());
    }

    @Override
    public ResponseBody generateCode(String email) {
        if (email == null) {
            return new ResponseBody(Code.BUSINESS_ERR, null, "Failed to send email because email address is null");
        }
        if (!assetHolderMapper.testEmailAddressExistence(email)) {
            return new ResponseBody(Code.BUSINESS_ERR, null, "Failed to send email because email address doesn't exist");
        }
        String code = String.valueOf(new Random().nextInt(899999) + 100000);
        saveEmailCode(email, code);
        sendVerificationEmail(email, code);
        return new ResponseBody(Code.SUCCESS, code, "Verification code has been sent to " + email);
    }

    public void saveEmailCode(String email, String code) {
        String redisKey = prefix + email;
        redisTemplate.opsForValue().set(redisKey, code, expireTime);
    }

    private void sendVerificationEmail(String email, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Verification your email!");

        message.setText("This verification code expires in 5 minutes."
                + System.lineSeparator()
                + verificationCode);
        mailSender.send(message);
    }

    @Override
    public ResponseBody validateCode(String email, String code) {
        String key = prefix + email;
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String savedCode = ops.get(key);
        if (savedCode == null) {
            return new ResponseBody(Code.BUSINESS_ERR, null, "Verification code doesn't exists or has expired!");
        }

        Long ttl = redisTemplate.getExpire(key);
        if (ttl == null || ttl <= 0) {
            return new ResponseBody(Code.BUSINESS_ERR, null, "Verification code has expired!");
        }

        if (!savedCode.equals(code)) {
            return new ResponseBody(Code.BUSINESS_ERR, null, "You entered wrong verification code!");
        }
        redisTemplate.delete(key);
        return new ResponseBody(Code.SUCCESS, null, "Verification code has been validated!");
    }

    @Override
    public ResponseBody registerGenerateCode(String email) {
        if (email == null) {
            return new ResponseBody(Code.BUSINESS_ERR, null, "Failed to send email because email address is null");
        }
        if (assetHolderMapper.testEmailAddressExistence(email)) {
            return new ResponseBody(Code.BUSINESS_ERR, null, "This email already registered! You can use this email to login");
        }
        String code = String.valueOf(new Random().nextInt(899999) + 100000);
        saveEmailCode(email, code);
        sendVerificationEmail(email, code);
        return new ResponseBody(Code.SUCCESS, code, "Verification code has been sent to " + email);
    }

    private String fillInVariablesWithValues(String emailContent, String assetId) {
        List<Asset> assetList = assetMapper.selectAssets(
                QueryTool.formatFilters(Map.of("asset_id", assetId)),
                null, null, null);
        String assetName = assetList.get(0).getName();
        List<AssetHolder> assetHolderList = assetHolderMapper.selectAssetHolders(
                QueryTool.formatFilters(Map.of("asset_holder_id", assetList.get(0).getOwnerId())),
                null, null, null);
        String contactName = assetHolderList.get(0).getName();

        String postTown = assetHolderMapper.selectAddressByAssetHolderId(assetHolderList.get(0).getAddressId()).get(0).get("city");

        emailContent = emailContent.replace("{{asset-model}}", assetName);
        emailContent = emailContent.replace("{{contact_name}}", contactName);
        emailContent = emailContent.replace("{{post_town}}", postTown);
        return emailContent;
    }
}
