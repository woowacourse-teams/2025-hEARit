package com.onair.hearit.admin.domain;

import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@RequiredArgsConstructor
public class AdminUserDetails implements UserDetails {

    private final Admin admin;

    @Override
    public String getUsername() {
        return admin.getLoginId();
    }

    @Override
    public String getPassword() {
        return admin.getPassword().getValue();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(); // 권한 필요 없음
    }
}
