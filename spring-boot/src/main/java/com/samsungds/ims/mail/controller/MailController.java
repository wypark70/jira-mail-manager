package com.samsungds.ims.mail.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MailController {
    @GetMapping("/mail")
    public String index() {
        return "index";
    }
}
