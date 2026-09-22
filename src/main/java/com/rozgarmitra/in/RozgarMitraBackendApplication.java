package com.rozgarmitra.in;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RozgarMitraBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RozgarMitraBackendApplication.class, args);
    }

}
