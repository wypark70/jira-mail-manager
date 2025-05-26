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
                AppUser appUser = userService.saveUser("wooyong.park", "jbj4a", Set.of("ROLE_ADMIN"), "wooyong.park@partner.samsung.com");
                log.info("Test user 'wooyong.park' registered successfully.");
            } catch (RuntimeException e) {
                log.error("Test user 'wooyong.park' already exists or failed to register: {}", e.getMessage());
            }

            try {
                AppUser appUser = userService.saveUser("kbeom.nam", "kbeom.nam", Set.of("ROLE_ADMIN", "ROLE_USER"), "kbeom.nam@samsung.com");
                log.info("Test admin 'kbeom.nam' registered successfully.");
            } catch (RuntimeException e) {
                log.error("Test admin 'kbeom.nam' already exists or failed to register: {}", e.getMessage());
            }

            try {
                AppUser appUser = userService.saveUser("yourim.an", "yourim.an", Set.of("ROLE_ADMIN", "ROLE_USER"), "yourim.an@samsung.com");
                log.info("Test admin 'yourim.an' registered successfully.");
            } catch (RuntimeException e) {
                log.error("Test admin 'yourim.an' already exists or failed to register: {}", e.getMessage());
            }

            try {
                AppUser appUser = userService.saveUser("admin", "admin", Set.of("ROLE_ADMIN", "ROLE_USER"), "admin@samsung.com");
                log.info("Test admin 'admin' registered successfully.");
            } catch (RuntimeException e) {
                log.error("Test admin 'admin' already exists or failed to register: {}", e.getMessage());
            }
        };
    }

}