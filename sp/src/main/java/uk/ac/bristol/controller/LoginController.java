package uk.ac.bristol.controller;

import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.AssetHolder;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.service.ContactService;
import uk.ac.bristol.service.UserService;
import uk.ac.bristol.util.JwtUtil;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class LoginController {

    private final UserService userService;
    private final ContactService contactService;

    public LoginController(UserService userService, ContactService contactService) {
        this.userService = userService;
        this.contactService = contactService;
    }

    @PostMapping("/login")
    public ResponseBody login(@RequestBody User user, HttpServletResponse response) {
        User u = userService.login(user);
        if (u == null) return new ResponseBody(Code.BUSINESS_ERR, null, "The username or password is incorrect.");
        JwtUtil.bindJWTAsCookie(response, u.getToken());
        u.setPassword(null);
        u.setToken(null);
        return new ResponseBody(Code.SUCCESS, u);
    }

    @PostMapping("/register")
    public ResponseBody register(@RequestBody Map<String, String> body) {
        String id = body.get("id");
        String password = body.get("password");
        String repassword = body.get("repassword");
        String name = body.get("name");
        String email = body.get("email");
        String phone = body.get("phone");

        if (id == null || id.isBlank()
                || password == null || password.isBlank()
                || repassword == null || repassword.isBlank()
                || name == null || name.isBlank()
                || email == null || email.isBlank()
                || phone == null || phone.isBlank()) {
            throw new SpExceptions.BusinessException("Key fields missing during registration.");
        }

        if(!password.equals(repassword)) {
            throw new SpExceptions.BusinessException("Two passwords don't match.");
        }

        AssetHolder ah = new AssetHolder();
        ah.setName(name);
        ah.setEmail(email);
        ah.setPhone(phone);
        User user = new User();
        user.setId(id);
        user.setPassword(password);
        user.setAssetHolder(ah);

        try {
            userService.registerNewUser(user);
            return new ResponseBody(Code.SUCCESS, null, "Success.");
        } catch (Exception e) {
            return new ResponseBody(Code.REGISTER_ERR, null, "Failed to register the user." + e.getMessage());
        }
    }

    @PostMapping("/email/code")
    public ResponseBody resetPasswordSendEmailWithCode(@RequestBody Map<String, String> body) {
        try {
            return contactService.generateCode(body.get("email"));
        } catch (Exception e) {
            return new ResponseBody(Code.REGISTER_ERR, null, "Failed to generate Code." + e.getMessage());
        }
    }

    @PostMapping("/email/verification")
    public ResponseBody resetPasswordValidateCode(@RequestBody Map<String, String> body) {
        return contactService.validateCode(body.get("email"), body.get("code"));
    }

    @PostMapping("/email/password")
    public ResponseBody resetPasswordUpdatePassword(@RequestBody Map<String, String> body) {
        try {
            userService.updatePasswordByEmail(body.get("email"), body.get("password"));
            return new ResponseBody(Code.SUCCESS, null, "Success.");
        } catch (Exception e) {
            return new ResponseBody(Code.BUSINESS_ERR, null, "Failed to update password.");
        }
    }

    @GetMapping("/exists/uid/{uid}")
    public ResponseBody checkUIDExistence(@PathVariable String uid) {
        if (userService.testUIDExistence(uid)) {
            return new ResponseBody(Code.SELECT_OK, null, "The user id already exists.");
        }
        return new ResponseBody(Code.SELECT_ERR, null, "The user id does not exist.");
    }

    @GetMapping("/exists/email/{email}")
    public ResponseBody checkEmailExistence(@PathVariable String email) {
        if (userService.testEmailExistence(email)) {
            return new ResponseBody(Code.SELECT_OK, null, "The email address already exists.");
        }
        return new ResponseBody(Code.SELECT_ERR, null, "The email address does not exist.");
    }
}
