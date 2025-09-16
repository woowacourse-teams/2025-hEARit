package com.onair.hearit.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Getter
@Embeddable
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Password {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Column(name = "password", nullable = false)
    private String value;

    public Password(String rawPassword) {
        this.value = encoder.encode(rawPassword);
    }
}
