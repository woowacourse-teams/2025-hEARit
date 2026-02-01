package com.onair.hearit.core.domain;

import com.onair.hearit.core.domain.exception.MemberDomainException;
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
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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

    @Column(columnDefinition = "BINARY(16)", nullable = false, unique = true)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID uuid;

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

    private Member(UUID uuid, String localId, String password, String socialId, String nickname, String profileImage,
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

    private void validate(UUID uuid, String nickname) {
        if (uuid == null) {
            throw new MemberDomainException("userUuid는 null일 수 없습니다.");
        }
        if (nickname == null) {
            throw new MemberDomainException("닉네임은 null이 될 수 없습니다.");
        }
    }

    public static Member createLocalUser(UUID uuid, String memberId, String nickname, String password,
                                         String profileImage) {
        return new Member(uuid, memberId, password, null, nickname, profileImage, OAuthProvider.NONE);
    }

    public static Member createSocialUser(UUID uuid, String socialId, String nickname, String profileImage,
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
        if (this.uuid == null || member.uuid == null) {
            return false;
        }
        return Objects.equals(uuid, member.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }
}
