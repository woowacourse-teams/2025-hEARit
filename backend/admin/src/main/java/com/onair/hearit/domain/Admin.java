package com.onair.hearit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "admin")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", nullable = false, unique = true)
    private String loginId;

    @Embedded
    private Password password;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    public Admin(String loginId, String rawPassword, String nickname) {
        validate(loginId, rawPassword, nickname);
        this.loginId = loginId;
        this.password = new Password(rawPassword);
        this.nickname = nickname;
    }

    private void validate(String loginId, String rawPassword, String nickname) {
        validateLoginId(loginId);
        validatePassword(rawPassword);
        validateNickname(nickname);
    }

    private void validateNickname(String nickname) {
        if (nickname == null) {
            throw new IllegalArgumentException("닉네임은 null이 될 수 없습니다.");
        }
    }

    private void validatePassword(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("비밀번호는 null이 될 수 없습니다.");
        }
    }

    void validateLoginId(String loginId) {
        if (loginId == null) {
            throw new IllegalArgumentException("로그인 id는 null이 될 수 없습니다.");
        }
    }
}
