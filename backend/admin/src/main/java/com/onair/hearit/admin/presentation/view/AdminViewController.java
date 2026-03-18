package com.onair.hearit.admin.presentation.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
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

    @GetMapping("/hearits-create")
    public String hearitCreatePage() {
        return "admin/hearit-create";
    }

    @GetMapping("/categories")
    public String categoryListPage() {
        return "admin/category-list";
    }

    @GetMapping("/keywords")
    public String keywordListPage() {
        return "admin/keyword-list";
    }

    @GetMapping("/recommend-hearit")
    public String recommendHearitPage() {
        return "admin/recommend-hearit";
    }

    @GetMapping("/elastic-search")
    public String tracePage() {
        return "admin/elastic-search";
    }
}
