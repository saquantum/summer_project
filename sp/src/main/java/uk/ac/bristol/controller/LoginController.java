package uk.ac.bristol.controller;

import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.service.ContactService;
import uk.ac.bristol.service.UserService;
import uk.ac.bristol.util.JwtUtil;

import javax.servlet.http.HttpServletResponse;

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
    public ResponseBody register(@RequestBody User user) {
        try {
            userService.registerNewUser(user);
            return new ResponseBody(Code.SUCCESS, null, "Success.");
        } catch (Exception e) {
            return new ResponseBody(Code.REGISTER_ERR, null, "Failed to register the user." + e.getMessage());
        }
    }

    @PostMapping("/reset/send")
    public ResponseBody reset(@RequestParam String email) {
        try {
            return contactService.generateCode(email);
        } catch (Exception e) {
            return new ResponseBody(Code.REGISTER_ERR, null, "Failed to generate Code." + e.getMessage());
        }
    }

    @PostMapping("/reset/validate")
    public ResponseBody validate(@RequestParam String email, @RequestParam String code) {
        return contactService.validateCode(email, code);
    }

    @PostMapping("/reset/commit")
    public ResponseBody commit(@RequestParam String password, @RequestParam String email) {
        try {
            userService.updatePasswordByEmail(email, password);
            return new ResponseBody(Code.SUCCESS, null, "Success.");
        } catch (Exception e) {
            return new ResponseBody(Code.BUSINESS_ERR, null, "Failed to update password.");
        }
    }
}
