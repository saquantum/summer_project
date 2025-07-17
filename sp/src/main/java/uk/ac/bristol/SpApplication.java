package uk.ac.bristol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SpApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpApplication.class, args);
    }

}
