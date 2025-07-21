package com.onair.hearit.admin.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminViewController {

    @GetMapping("/hearits/upload")
    public String hearitUploadForm() {
        return "admin/hearit-upload";
    }

    @GetMapping
    public String adminHome() {
        return "admin/home";
    }
}
