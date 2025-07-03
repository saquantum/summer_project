package uk.ac.bristol.service.impl;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.bristol.controller.Code;
import uk.ac.bristol.controller.ResponseBody;
import uk.ac.bristol.dao.AssetHolderMapper;
import uk.ac.bristol.dao.AssetMapper;
import uk.ac.bristol.dao.WarningMapper;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.*;
import uk.ac.bristol.service.ContactService;
import uk.ac.bristol.service.UserService;
import uk.ac.bristol.util.JwtUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
@Service
public class ContactServiceImpl implements ContactService {
    @Value("${spring.mail.username}")
    private String from;

    @Value("${app.base-url}")
    private String baseURL;

    private final Map<String, String> codeStore = new ConcurrentHashMap<>();
    private final Map<String, Long> timestampStore = new ConcurrentHashMap<>();
    private static final long EXPIRE_TIME = 5L * 60 * 1000;

    private final JavaMailSender mailSender;

    private final UserService userService;
    private final WarningMapper warningMapper;
    private final AssetMapper assetMapper;
    private final AssetHolderMapper assetHolderMapper;

    public ContactServiceImpl(JavaMailSender mailSender, UserService userService, WarningMapper warningMapper, AssetMapper assetMapper, AssetHolderMapper assetHolderMapper) {
        this.mailSender = mailSender;
        this.userService = userService;
        this.warningMapper = warningMapper;
        this.assetMapper = assetMapper;
        this.assetHolderMapper = assetHolderMapper;
    }

    @Override
    public Map<String, Object> formatNotification(Long warningId, String assetId) {
        Boolean test = warningMapper.testIfGivenAssetIntersectsWithWarning(assetId, warningId);
        if (test == null || !test) {
            return null;
        }

        List<Warning> warning = warningMapper.selectWarningById(warningId);
        if (warning.size() != 1) {
            throw new SpExceptions.GetMethodException("Get " + warning.size() + " warnings using id " + warningId);
        }

        String typeId = assetMapper.selectAssetTypeByID(assetId);
        String warningType = warning.get(0).getWeatherType();
        String severity = warning.get(0).getWarningLevel();

        List<Template> template = warningMapper.selectNotificationTemplateByTypes(typeId, warningType, severity);
        if (template.size() != 1) {
            throw new SpExceptions.GetMethodException("Get " + template.size() + " templates using id " + warningId);
        }

        String message = template.get(0).getMessage();

        if (message == null) {
            throw new SpExceptions.GetMethodException("The message type you required does not exist");
        }

        String assetOwnerId = assetMapper.selectAssetOwnerIdByAssetId(assetId);

        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        return new HashMap<>(Map.of(
                "id", id,
                "assetTypeId", typeId,
                "toOwnerId", assetOwnerId,
                "weatherWarningType", warningType,
                "severity", severity,
                "message", message,
                "createdAt", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "channel", ""));
    }

    @Override
    public ResponseBody sendEmail(Map<String, Object> notification) {
        if (notification == null) {
            return new ResponseBody(Code.BUSINESS_ERR, null, "Failed to send email because notification is null");
        }
        List<Map<String, Object>> contactPreferences = assetHolderMapper.selectContactPreferencesByAssetHolderId(notification.get("toOwnerId").toString());
        if (contactPreferences.size() != 1) {
            throw new SpExceptions.GetMethodException("The database might be modified by another transaction");
        }

        if ((Boolean) contactPreferences.get(0).get("email") == false) {
            return new ResponseBody(Code.SUCCESS, null, "The asset holder prefers not to receive by email");
        }

        List<AssetHolder> holder = assetHolderMapper.selectAssetHolderByIDs(List.of(notification.get("toOwnerId").toString()), null, null, null);
        if (holder.size() != 1) {
            throw new SpExceptions.GetMethodException("The database might be modified by another transaction");
        }

        String emailAddress = holder.get(0).getEmail();
        sendEmailToAddress(emailAddress, notification.toString());
        return new ResponseBody(Code.SUCCESS, null, "The email has been sent to " + emailAddress);
    }

    private void sendEmailToAddress(String toEmailAddress, String emailContent) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmailAddress);
        message.setSubject("Weather Warning Notifications");

        Map<String, Object> claims = new HashMap<>();
        claims.put("unsubscribe-email-uid", toEmailAddress);
        claims.put("action", "unsubscribe-email");
        String unsubscribeUrl = baseURL + "/user/notify/email/unsubscribe?token=" + JwtUtil.generateJWT(claims);
        message.setText(emailContent
                + System.lineSeparator()
                + System.lineSeparator()
                + "To unsubscribe email notifications, click the link below:"
                + System.lineSeparator()
                + unsubscribeUrl);
        mailSender.send(message);
    }

    @Override
    public ResponseBody unsubscribeEmail(String token) {
        Claims claims = JwtUtil.parseJWT(token);
        if (!"unsubscribe-email".equals(claims.get("action"))) {
            throw new SpExceptions.BusinessException("The token provided is incorrect to unsubscribe email");
        }
        String uid = claims.get("unsubscribe-email-uid").toString();

        User user = userService.getUserByUserId(uid);
        AssetHolder ah = user.getAssetHolder();
        if (ah == null) {
            throw new SpExceptions.BusinessException("The user has no active asset holder details");
        }

        ah.getContactPreferences().put("email", false);
        return new ResponseBody(Code.DELETE_OK, null, "Successfully unsubscribed email for user " + uid);
    }

//    @Override
//    public ResponseBody sendDiscordToAddress(String whatsappMessage, String url) {
//
//    }

    @Override
    public ResponseBody generateCode(String email) {
        if (email == null) {
            return new ResponseBody(Code.BUSINESS_ERR, null, "Failed to send email because email address is null");
        }
        if (!assetHolderMapper.testEmailAddressExistence(email)) {
            return new ResponseBody(Code.BUSINESS_ERR, null, "Failed to send email because email address doesn't exist");
        }
        String code = String.valueOf(new Random().nextInt(899999) + 100000);
        codeStore.put(email, code);
        timestampStore.put(email, System.currentTimeMillis());
        sendVerificationEmail(email, code);
        return new ResponseBody(Code.SUCCESS, null, "Verification code has been sent to " + email);
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
        String savedCode = codeStore.get(email);
        Long time = timestampStore.get(email);
        if (savedCode == null || time == null) {
            return new ResponseBody(Code.BUSINESS_ERR, null, "Verification code doesn't exists!");
        }
        if (System.currentTimeMillis() - time > EXPIRE_TIME) {
            return new ResponseBody(Code.BUSINESS_ERR, null, "Verification code has expired!");
        }
        if (savedCode.equals(code)) {
            return new ResponseBody(Code.SUCCESS, null, "Verification code has been validated!");
        } else {
            return new ResponseBody(Code.BUSINESS_ERR, null, "You entered wrong verification code!");
        }
    }
}
