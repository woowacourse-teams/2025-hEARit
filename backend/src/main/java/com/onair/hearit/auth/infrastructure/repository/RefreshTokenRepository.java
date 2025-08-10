package com.onair.hearit.auth.infrastructure.repository;

import com.onair.hearit.auth.domain.RefreshToken;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByMemberId(Long memberId);

    Optional<RefreshToken> findByToken(String token);

    @Transactional
    void deleteByMemberId(Long memberId);
}
