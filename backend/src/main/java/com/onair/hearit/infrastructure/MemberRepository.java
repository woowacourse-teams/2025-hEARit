package com.onair.hearit.infrastructure;

import com.onair.hearit.auth.application.OAuthProvider;
import com.onair.hearit.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("SELECT m FROM Member m WHERE m.localId = :localId AND m.deletedAt IS NULL")
    Optional<Member> findByLocalId(String localId);

    @Query("SELECT m FROM Member m WHERE m.socialId = :socialId AND m.oAuthProvider = :provider AND m.deletedAt IS NULL")
    Optional<Member> findBySocialIdAndOAuthProvider(String socialId, OAuthProvider provider);

    @Query("SELECT count(m) > 0 FROM Member m WHERE m.localId = :localId AND m.deletedAt IS NULL")
    boolean existsByLocalId(String localId);
}
