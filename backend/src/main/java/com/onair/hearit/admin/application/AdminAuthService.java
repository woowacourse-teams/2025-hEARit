package com.onair.hearit.admin.application;

import com.onair.hearit.admin.dto.AdminLoginRequest;
import com.onair.hearit.admin.infrastructure.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminRepository adminRepository;

    public void login(AdminLoginRequest request) {

    }
}
