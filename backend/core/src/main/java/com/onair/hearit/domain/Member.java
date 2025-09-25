package com.onair.hearit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member",
        uniqueConstraints = {
                @UniqueConstraint(name = "social_member_unique_constraint", columnNames = {
                        "social_id", "oauth_provider", "deleted_at"
                })
        })
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true, length = 36)
    private String uuid;

    @Column(name = "local_id")
    private String localId; // 자체 회원용

    @Column(name = "password")
    private String password; // 자체 회원용

    @Column(name = "social_id")
    private String socialId;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "oauth_provider")
    @Enumerated(value = EnumType.STRING)
    private OAuthProvider oAuthProvider;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    private Member(String uuid, String localId, String password, String socialId, String nickname, String profileImage,
                   OAuthProvider provider) {
        validate(uuid, nickname);
        this.uuid = uuid;
        this.localId = localId;
        this.password = password;
        this.socialId = socialId;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.oAuthProvider = provider;
    }

    private void validate(String uuid, String nickname) {
        if (uuid == null) {
            throw new IllegalArgumentException("userUuid는 null일 수 없습니다.");
        }
        if (uuid.length() != 36) {
            throw new IllegalArgumentException("userUuid 형식이 올바르지 않습니다.");
        }
        if (nickname == null) {
            throw new IllegalArgumentException("닉네임은 null이 될 수 없습니다.");
        }
    }

    public static Member createLocalUser(String uuid, String memberId, String nickname, String password,
                                         String profileImage) {
        return new Member(uuid, memberId, password, null, nickname, profileImage, OAuthProvider.NONE);
    }

    public static Member createSocialUser(String uuid, String socialId, String nickname, String profileImage,
                                          OAuthProvider provider) {
        return new Member(uuid, null, null, socialId, nickname, profileImage, provider);
    }

    public void withdraw() {
        this.deletedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Member member)) {
            return false;
        }
        if (this.id == null || member.id == null) {
            return false;
        }
        return Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
