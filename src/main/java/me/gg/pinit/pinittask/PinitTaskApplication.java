package me.gg.pinit.pinittask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PinitTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(PinitTaskApplication.class, args);
    }

}
