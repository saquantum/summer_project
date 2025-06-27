package uk.ac.bristol.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DummyController {
    @GetMapping("/hello")
    public String api(){
        return "Hello World";
    }

    @GetMapping("/user/hello")
    public String user(){
        return "Hello World";
    }

    @GetMapping("/admin/hello")
    public String admin(){
        return "Hello World";
    }

    @GetMapping("/asset/hello")
    public String asset(){
        return "Hello World";
    }

    @GetMapping("/warning/hello")
    public String warning(){
        return "Hello World";
    }
}
