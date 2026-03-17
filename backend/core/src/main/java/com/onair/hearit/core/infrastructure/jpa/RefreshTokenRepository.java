package com.onair.hearit.core.infrastructure.jpa;

import com.onair.hearit.core.domain.RefreshToken;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByMemberUuid(UUID memberUuid);

    Optional<RefreshToken> findByToken(String token);

    @Query("""
            SELECT rt.memberUuid
            FROM RefreshToken rt
            WHERE rt.expiryDate > :now
            """)
    List<UUID> findActiveMemberUuids(@Param("now") LocalDateTime now);

    @Transactional
    void deleteByMemberUuid(UUID memberUuid);
}
