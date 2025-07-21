package com.onair.hearit.infrastructure;

import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Member;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Page<Bookmark> findAllByMemberOrderByCreatedAtDesc(Member member, Pageable pageable);

    boolean existsByHearitIdAndMemberId(Long hearitId, Long memberId);

    Optional<Bookmark> findByHearitIdAndMemberId(Long hearitId, Long memberId);
}
