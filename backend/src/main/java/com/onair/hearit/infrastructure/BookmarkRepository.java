package com.onair.hearit.infrastructure;

import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Member;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    List<Bookmark> findAllByMemberOrderByCreatedAtDesc(Member member, Pageable pageable);

    boolean existsByHearitIdAndMemberId(Long hearitId, Long memberId);
}
