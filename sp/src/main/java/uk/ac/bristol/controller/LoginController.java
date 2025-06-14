package uk.ac.bristol.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.service.SqlService;

import javax.servlet.http.HttpServletResponse;
import java.time.Duration;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class LoginController {

    @Autowired
    private SqlService sqlService;

    @PostMapping("/login")
    public ResponseResult login(@RequestBody User user, HttpServletResponse response) {
        User u = sqlService.login(user);
        if (u == null) return new ResponseResult(Code.BUSINESS_ERR, null, "The username or password is incorrect.");

        ResponseCookie cookie = ResponseCookie.from("token", u.getToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofHours(24))
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        u.setPassword(null);
        u.setToken(null);

        return new ResponseResult(Code.SUCCESS, u);
    }
}
