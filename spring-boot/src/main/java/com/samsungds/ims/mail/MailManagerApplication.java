package com.samsungds.ims.mail;

import com.samsungds.ims.mail.model.AppUser;
import com.samsungds.ims.mail.service.AppUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Set;

@SpringBootApplication
@Slf4j
public class MailManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MailManagerApplication.class, args);
    }

    @Bean
    public CommandLineRunner addDefaultUser(AppUserService userService) {
        return (args) -> {
            try {
                // "user" / "password" 사용자 등록
                AppUser appUser = userService.saveUser("user", "user", Set.of("ROLE_USER"), "user@samsung.com");
                log.info("Test user 'user' registered successfully.");
            } catch (RuntimeException e) {
                log.error("Test user 'user' already exists or failed to register: {}", e.getMessage());
            }

            try {
                // "admin" / "adminpass" 관리자 사용자 등록
                AppUser appUser = userService.saveUser("admin", "admin", Set.of("ROLE_ADMIN", "ROLE_USER"), "admin@samsung.com");
                log.info("Test admin 'admin' registered successfully.");
            } catch (RuntimeException e) {
                log.error("Test admin 'admin' already exists or failed to register: {}", e.getMessage());
            }
        };
    }

}