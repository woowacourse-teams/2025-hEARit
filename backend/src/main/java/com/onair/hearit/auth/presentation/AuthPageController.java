package com.onair.hearit.auth.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthPageController {

    @GetMapping("/admin/login")
    public String showLoginPage() {
        return "admin/login";
    }
}
