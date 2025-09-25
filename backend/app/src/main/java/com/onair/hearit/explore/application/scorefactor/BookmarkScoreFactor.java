package com.onair.hearit.explore.application.scorefactor;

import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Member;
import com.onair.hearit.domain.UserType;
import com.onair.hearit.exception.custom.NotFoundException;
import com.onair.hearit.infrastructure.jpa.BookmarkRepository;
import com.onair.hearit.infrastructure.jpa.CategoryBookmarkCount;
import com.onair.hearit.infrastructure.jpa.MemberRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookmarkScoreFactor implements ScoreFactor {

    private static final double MAX_BOOKMARK_SCORE = 30.0;
    private static final double MIN_BOOKMARK_SCORE = 0.0;

    private final MemberRepository memberRepository;
    private final BookmarkRepository bookmarkRepository;

    private static long calculateTotalBookmarkCount(Map<Long, Long> bookmarkCountsByCategory) {
        return Math.max(1, bookmarkCountsByCategory.values().stream()
                .mapToLong(Long::longValue)
                .sum());
    }

    private static double calculateBookmarkScore(Hearit hearit,
                                                 Map<Long, Long> bookmarkCountsByCategory,
                                                 long totalBookmarkCount) {
        long sameCategoryCount = bookmarkCountsByCategory
                .getOrDefault(hearit.getCategory().getId(), 0L);
        return Math.max(MIN_BOOKMARK_SCORE, ((double) sameCategoryCount / totalBookmarkCount) * MAX_BOOKMARK_SCORE);
    }

    @Override
    public boolean isSupported(UserType userType) {
        return userType == UserType.MEMBER;
    }

    @Override
    public Map<Long, Double> calculate(String uuid, List<Hearit> hearits) {
        Member member = getMemberByUuid(uuid);
        Map<Long, Long> bookmarkCountsByCategory = getBookmarkCountsByCategory(member.getId());
        long totalBookmarkCount = calculateTotalBookmarkCount(bookmarkCountsByCategory);
        return hearits.stream()
                .collect(Collectors.toMap(
                        Hearit::getId,
                        hearit -> calculateBookmarkScore(hearit, bookmarkCountsByCategory, totalBookmarkCount)
                ));
    }

    private Member getMemberByUuid(String uuid) {
        return memberRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("uuid", uuid));
    }

    private Map<Long, Long> getBookmarkCountsByCategory(Long memberId) {
        return bookmarkRepository.countMemberBookmarksByCategoryId(memberId).stream()
                .collect(Collectors.toMap(
                        CategoryBookmarkCount::getCategoryId,
                        CategoryBookmarkCount::getCount
                ));
    }
}
