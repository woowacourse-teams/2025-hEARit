package com.onair.hearit.core.infrastructure.jpa;

import com.onair.hearit.core.domain.RefreshToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByMemberUuid(UUID memberUuid);

    Optional<RefreshToken> findByToken(String token);

    @Transactional
    void deleteByMemberUuid(UUID memberUuid);
}
