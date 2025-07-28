package com.onair.hearit.auth.application;

import com.onair.hearit.admin.domain.Admin;
import com.onair.hearit.admin.domain.AdminUserDetails;
import com.onair.hearit.admin.infrastructure.AdminRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;

    public AdminUserDetailsService(AdminRepository repo) {
        this.adminRepository = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String loginId) {
        Admin admin = adminRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 관리자입니다."));
        return new AdminUserDetails(admin);
    }
}
