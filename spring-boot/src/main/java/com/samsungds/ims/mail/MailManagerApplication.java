package com.samsungds.ims.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class MailManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MailManagerApplication.class, args);
    }
}