package com.onair.hearit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Table(name = "member")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    public Member(String localId, String password, String socialId, String nickname,
                  MemberRole role) {
        this.localId = localId;
        this.password = password;
        this.socialId = socialId;
        this.nickname = nickname;
        this.role = role;
    }

    public static Member createAdmin(String memberId, String nickname, String password) {
        return new Member(memberId, password, null, nickname, MemberRole.ADMIN);
    }

    public static Member createLocalUser(String memberId, String nickname, String password) {
        return new Member(memberId, password, null, nickname, MemberRole.USER);
    }

    public static Member createSocialUser(String socialId,  String nickname) {
        return new Member(null, null, socialId, nickname, MemberRole.USER);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "local_id")
    private String localId; // 자체 회원용

    @Column(name = "password")
    private String password; // 자체 회원용

    @Column(name = "social_id")
    private String socialId;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "role", nullable = false)
    private MemberRole role;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
