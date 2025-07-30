package com.onair.hearit.admin.presentation;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Hidden
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminViewController {

    @GetMapping
    public String adminHome() {
        return "admin/home";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "admin/login";
    }

    @GetMapping("/hearits")
    public String hearitListPage() {
        return "admin/hearit-list";
    }

    @GetMapping("/categories")
    public String categoryListPage() {
        return "admin/category-list";
    }

    @GetMapping("/keywords")
    public String keywordListPage() {
        return "admin/keyword-list";
    }
}
