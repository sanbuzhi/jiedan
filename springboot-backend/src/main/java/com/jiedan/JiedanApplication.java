package com.jiedan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class JiedanApplication {

    public static void main(String[] args) {
        SpringApplication.run(JiedanApplication.class, args);
    }

}
