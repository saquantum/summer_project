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
import uk.ac.bristol.dao.ContactMapper;
import uk.ac.bristol.dao.MetaDataMapper;
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
    private final MetaDataMapper metaDataMapper;
    private final ContactMapper contactMapper;

    private final Duration expireTime = Duration.ofMinutes(5);
    private final String prefix = "email:verify:code:";

    public ContactServiceImpl(UserService userService, MetaDataMapper metaDataMapper, ContactMapper contactMapper) {
        this.userService = userService;
        this.metaDataMapper = metaDataMapper;
        this.contactMapper = contactMapper;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void sendNotificationsToUser(Warning warning, UserWithAssets uwa) {
        Map<String, Object> contactPreferences = uwa.getUser().getAssetHolder().getContactPreferences();

        Map<String, Object> emailNotification = formatNotification(warning, uwa, "email");
        if (contactPreferences.get("email").equals(Boolean.TRUE)) {
            sendEmailToAddress(
                    uwa.getUser().getAssetHolder().getEmail(),
                    emailNotification
            );
        }
        if (contactPreferences.get("phone").equals(Boolean.TRUE)) {
            Map<String, Object> smsNotification = formatNotification(warning, uwa, "phone");
            // send sms
        }
        if (contactPreferences.get("post").equals(Boolean.TRUE)) {
            Map<String, Object> postNotification = formatNotification(warning, uwa, "post");
            // send http post
        }

        // send inbox notification using email message
        insertInboxMessageToUser(Map.of(
                "userId", emailNotification.get("toUserId"),
                "hasRead", false,
                "issuedDate", emailNotification.get("createdAt"),
                "validUntil", emailNotification.get("validUntil"),
                "title", emailNotification.get("title"),
                "message", emailNotification.get("body")));
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public Map<String, Object> formatNotification(Warning warning, UserWithAssets uwa, String channel) {
        // 1. get all templates for given warning
        String warningType = warning.getWeatherType();
        String severity = warning.getWarningLevel();

        List<Template> allTemplatesOfWarning = contactMapper.selectNotificationTemplateByTypes(new Template(null, warningType, severity, channel));
        if (allTemplatesOfWarning.isEmpty()) {
            throw new SpExceptions.SystemException("No templates found for the type of warning " + warning.getId());
        }
        Map<String, Template> mapping = new HashMap<>();
        for (Template template : allTemplatesOfWarning) {
            mapping.put(template.getAssetTypeId(), template);
        }

        // 2. get individual template for each asset and group them together
        int length = uwa.getAssets().size();
        StringBuilder title = new StringBuilder();
        StringBuilder body = new StringBuilder();
        for (Asset asset : uwa.getAssets()) {
            String typeId = asset.getTypeId();
            if (!mapping.containsKey(typeId)) {
                throw new SpExceptions.SystemException("Template for Asset type " + typeId + " does not exist");
            }
            Template template = mapping.get(typeId);
            if (length == 1) {
                title.append(fillInVariablesWithValues(template.getTitle(), uwa.getUser(), asset));
            }
            body.append(fillInVariablesWithValues(template.getBody(), uwa.getUser(), asset)).append(System.lineSeparator());
        }
        if (length > 1) {
            title.append("Weather warning for multiple assets");
        }

        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        return new HashMap<>(Map.of(
                "id", id,
                "toUserId", uwa.getUser().getId(),
                "weatherWarningType", warningType,
                "severity", severity,
                "title", title.toString(),
                "body", body.toString(),
                "createdAt", now,
                "validUntil", warning.getValidTo(),
                "channel", channel));
    }

    private String fillInVariablesWithValues(String content, User user, Asset asset) {
        return content
                .replace("{{asset-model}}", asset.getName())
                .replace("{{contact_name}}", user.getAssetHolder().getName())
                .replace("{{post_town}}", user.getAssetHolder().getAddress().get("city"));
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public void sendEmailToAddress(String toEmailAddress, Map<String, Object> notification) {
        if (notification == null) {
            throw new SpExceptions.SystemException("Failed to send email because notification is null");
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper mmh;
        try {
            mmh = new MimeMessageHelper(message, true, "UTF-8");
            mmh.setFrom(from);
            mmh.setTo(toEmailAddress);
            mmh.setSubject(notification.get("title").toString());

            Map<String, Object> claims = new HashMap<>();
            claims.put("unsubscribe-email-uid", notification.get("toUserId").toString());
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
                    notification.get("body").toString()
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

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
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
        userService.updateAssetHolder(ah);
        return new ResponseBody(Code.DELETE_OK, null, "Successfully unsubscribed email for user " + uid);
    }

    @Value("${twilio.account-sid}")
    private String twilioAccountSid;
    @Value("${twilio.auth-token}")
    private String twilioAuthToken;
    @Value("${twilio.from-number}")
    private String fromPhoneNumber;

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
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

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public ResponseBody generateCode(String email) {
        if (email == null) {
            return new ResponseBody(Code.BUSINESS_ERR, null, "Failed to send email because email address is null");
        }
        if (!userService.testEmailAddressExistence(email)) {
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

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
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

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public ResponseBody registerGenerateCode(String email) {
        if (email == null) {
            return new ResponseBody(Code.BUSINESS_ERR, null, "Failed to send email because email address is null");
        }
        if (userService.testEmailAddressExistence(email)) {
            return new ResponseBody(Code.BUSINESS_ERR, null, "This email already registered! You can use this email to login");
        }
        String code = String.valueOf(new Random().nextInt(899999) + 100000);
        saveEmailCode(email, code);
        sendVerificationEmail(email, code);
        return new ResponseBody(Code.SUCCESS, code, "Verification code has been sent to " + email);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<Template> getAllNotificationTemplates(Map<String, Object> filters,
                                                      List<Map<String, String>> orderList,
                                                      Integer limit,
                                                      Integer offset) {
        return contactMapper.selectAllNotificationTemplates(
                QueryTool.formatFilters(filters),
                QueryTool.filterOrderList(orderList, "templates"),
                limit, offset);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<Template> getNotificationTemplateByTypes(Template template) {
        return contactMapper.selectNotificationTemplateByTypes(template);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public List<Template> getNotificationTemplateById(Long id) {
        return contactMapper.selectNotificationTemplateById(id);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int insertNotificationTemplate(Template templates) {
        int n = contactMapper.insertNotificationTemplate(templates);
        metaDataMapper.increaseTotalCountByTableName("templates", n);
        return n;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int updateNotificationTemplateMessageById(Template template) {
        return contactMapper.updateNotificationTemplateMessageById(template);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int updateNotificationTemplateMessageByTypes(Template template) {
        return contactMapper.updateNotificationTemplateMessageByTypes(template);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteNotificationTemplateByIds(Long[] ids) {
        int n = contactMapper.deleteNotificationTemplateByIds(ids);
        metaDataMapper.increaseTotalCountByTableName("templates", -n);
        return n;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteNotificationTemplateByIds(List<Long> ids) {
        int n = contactMapper.deleteNotificationTemplateByIds(ids);
        metaDataMapper.increaseTotalCountByTableName("templates", -n);
        return n;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteNotificationTemplateByType(Template template) {
        int n = contactMapper.deleteNotificationTemplateByType(template);
        metaDataMapper.increaseTotalCountByTableName("templates", -n);
        return n;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    @Override
    public Map<String, Object> getUserInboxMessagesByUserId(String userId) {
        return contactMapper.selectUserInboxMessagesByUserId(userId);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int insertInboxMessageToUser(Map<String, Object> message) {
        return contactMapper.insertInboxMessageToUser(message);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Override
    public int updateInboxMessageByUserId(Map<String, Object> message) {
        return contactMapper.updateInboxMessageByUserId(message);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteInboxMessageByFilter(Map<String, Object> filters) {
        return contactMapper.deleteInboxMessageByFilter(QueryTool.formatFilters(filters));
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteOutDatedInboxMessages() {
        return contactMapper.deleteOutDatedInboxMessages();
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public int deleteOutDatedInboxMessagesByUserId(String userId) {
        return contactMapper.deleteOutDatedInboxMessagesByUserId(userId);
    }
}
