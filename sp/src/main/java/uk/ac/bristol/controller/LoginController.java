package uk.ac.bristol.controller;

import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.service.ContactService;
import uk.ac.bristol.service.TokenBlacklistService;
import uk.ac.bristol.service.UserService;
import uk.ac.bristol.util.JwtUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class LoginController {

    private final UserService userService;
    private final ContactService contactService;
    private final TokenBlacklistService tokenBlacklistService;

    public LoginController(UserService userService, ContactService contactService, TokenBlacklistService tokenBlacklistService) {
        this.userService = userService;
        this.contactService = contactService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/login")
    public ResponseBody login(@RequestBody User user, HttpServletResponse response) {
        JwtUtil.bindJWTAsCookie(response, userService.login(user));
        return new ResponseBody(Code.SUCCESS, null);
    }

    @PostMapping("/register/email/code")
    public ResponseBody registerSendEmailWithCode(@RequestBody Map<String, String> body) {
        return contactService.registerGenerateCode(body.get("email"));
    }

    @PostMapping("/register")
    public ResponseBody register(@RequestBody Map<String, String> body) {
        userService.registerNewUser(body);
        return new ResponseBody(Code.SUCCESS, null, "Success.");
    }

    @PostMapping("/email/code")
    public ResponseBody resetPasswordSendEmailWithCode(@RequestBody Map<String, String> body) {
        return contactService.generateCode(body.get("email"));
    }

    @PostMapping("/email/verification")
    public ResponseBody resetPasswordValidateCode(@RequestBody Map<String, String> body) {
        return contactService.validateCode(body.get("email"), body.get("code"));
    }

    @PostMapping("/email/password")
    public ResponseBody resetPasswordUpdatePassword(@RequestBody Map<String, String> body) {
        userService.updateUserPasswordByEmail(body.get("email"), body.get("password"));
        return new ResponseBody(Code.SUCCESS, null, "Success.");
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
        if (userService.testEmailAddressExistence(email)) {
            return new ResponseBody(Code.SELECT_OK, null, "The email address already exists.");
        }
        return new ResponseBody(Code.SELECT_ERR, null, "The email address does not exist.");
    }

    @PostMapping("/logout")
    public ResponseBody logout(HttpServletRequest request) {
        try {
            String token = JwtUtil.getJWTFromCookie(request);
            if (token != null) {
                tokenBlacklistService.blacklistToken(token);
                return new ResponseBody(Code.SUCCESS, null, "Logged out successfully");
            }
            return new ResponseBody(Code.BUSINESS_ERR, null, "No token found");
        } catch (Exception e) {
            return new ResponseBody(Code.BUSINESS_ERR, null, "Logout failed: " + e.getMessage());
        }
    }
}
