package com.onair.hearit.core.domain;

import com.onair.hearit.core.domain.exception.RefreshTokenDomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Table(name = "refresh_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_uuid", columnDefinition = "BINARY(16)", nullable = false, unique = true)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID memberUuid;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    public RefreshToken(UUID memberUuid, String token, LocalDateTime expiryDate) {
        validate(memberUuid, token, expiryDate);
        this.memberUuid = memberUuid;
        this.token = token;
        this.expiryDate = expiryDate;
    }

    private void validate(UUID memberUuid, String token, LocalDateTime expiryDate) {
        validateMember(memberUuid);
        validateToken(token);
        validateExpiryDate(expiryDate);
    }

    private void validateExpiryDate(LocalDateTime expiryDate) {
        if (expiryDate == null) {
            throw new RefreshTokenDomainException("만료일은 null이 될 수 없습니다.");
        }
    }

    private void validateToken(String token) {
        if (token == null) {
            throw new RefreshTokenDomainException("리프레시 토큰은 null이 될 수 없습니다.");
        }
    }

    private void validateMember(UUID memberUuid) {
        if (memberUuid == null) {
            throw new RefreshTokenDomainException("멤버는 null이 될 수 없습니다.");
        }
    }


    public void update(String token, LocalDateTime expiryDate) {
        validateToken(token);
        validateExpiryDate(expiryDate);
        this.token = token;
        this.expiryDate = expiryDate;
    }
}
