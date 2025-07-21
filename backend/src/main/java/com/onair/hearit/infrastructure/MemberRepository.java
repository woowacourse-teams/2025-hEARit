package com.onair.hearit.infrastructure;

import com.onair.hearit.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByLocalId(String memberId);

    boolean existsByLocalId(String memberId);

    Optional<Member> findBySocialId(String socialId);
}
