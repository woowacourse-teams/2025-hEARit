package com.onair.hearit.core.infrastructure.jpa;

import com.onair.hearit.core.domain.Member;
import com.onair.hearit.core.domain.OAuthProvider;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("SELECT m FROM Member m WHERE m.localId = :localId AND m.deletedAt IS NULL")
    Optional<Member> findByLocalId(String localId);

    @Query("SELECT m FROM Member m WHERE m.socialId = :socialId AND m.oAuthProvider = :provider AND m.deletedAt IS NULL")
    Optional<Member> findBySocialIdAndOAuthProvider(String socialId, OAuthProvider provider);

    @Query("SELECT count(m) > 0 FROM Member m WHERE m.localId = :localId AND m.deletedAt IS NULL")
    boolean existsByLocalId(String localId);

    @Query("SELECT m FROM Member m WHERE m.uuid = :uuid AND m.deletedAt IS NULL")
    Optional<Member> findByUuid(UUID uuid);

    @Query("SELECT m FROM Member m WHERE m.uuid IN :uuids")
    List<Member> findAllByUuidIn(@Param("uuids") Set<UUID> uuids);
}
