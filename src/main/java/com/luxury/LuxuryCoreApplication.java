package com.luxury;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LuxuryCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(LuxuryCoreApplication.class, args);
    }
}
