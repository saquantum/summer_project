package uk.ac.bristol.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@CrossOrigin
public class Pages {

    @GetMapping("/")
    public void redirectHome(HttpServletResponse response) throws IOException {
        response.sendRedirect("/index.html");
    }

    @GetMapping("/user")
    public void redirectUser(HttpServletResponse response) throws IOException {
        response.sendRedirect("/user/index.html");
    }

    @GetMapping("/admin")
    public void redirectAdmin(HttpServletResponse response) throws IOException {
        response.sendRedirect("/admin/index.html");
    }
}
