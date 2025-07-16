package com.onair.hearit.infrastructure;

import com.onair.hearit.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
