package uk.ac.bristol.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uk.ac.bristol.pojo.User;
import uk.ac.bristol.service.SqlService;
import uk.ac.bristol.util.JwtUtil;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/login")
@CrossOrigin
public class LoginController {

    @Autowired
    private SqlService sqlService;

    @PostMapping()
    public ResponseResult login(@RequestBody User user, HttpServletResponse response) {
        User u = sqlService.login(user);
        if (u == null) return new ResponseResult(Code.BUSINESS_ERR, null, "The username or password is incorrect.");
        JwtUtil.bindJWTAsCookie(response,u.getToken());
        u.setPassword(null);
        u.setToken(null);
        return new ResponseResult(Code.SUCCESS, u);
    }
}
