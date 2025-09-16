package com.onair.hearit.auth.domain;

import com.onair.hearit.common.exception.custom.InvalidInputException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "refresh_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false, unique = true)
    private Long memberId;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    public RefreshToken(Long memberId, String token, LocalDateTime expiryDate) {
        validate(memberId, token, expiryDate);
        this.memberId = memberId;
        this.token = token;
        this.expiryDate = expiryDate;
    }

    private void validate(Long memberId, String token, LocalDateTime expiryDate) {
        validateMember(memberId);
        validateToken(token);
        validateExpiryDate(expiryDate);
    }

    private void validateExpiryDate(LocalDateTime expiryDate) {
        if (expiryDate == null) {
            throw new InvalidInputException("멤버는 null이 될 수 없습니다.");
        }
    }

    private void validateToken(String token) {
        if (token == null) {
            throw new InvalidInputException("멤버는 null이 될 수 없습니다.");
        }
    }

    private void validateMember(Long memberId) {
        if (memberId == null) {
            throw new InvalidInputException("멤버는 null이 될 수 없습니다.");
        }
    }


    public void update(String token, LocalDateTime expiryDate) {
        validateToken(token);
        validateExpiryDate(expiryDate);
        this.token = token;
        this.expiryDate = expiryDate;
    }
}
