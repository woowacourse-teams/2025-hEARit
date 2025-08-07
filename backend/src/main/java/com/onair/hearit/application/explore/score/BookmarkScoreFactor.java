package com.onair.hearit.application.explore.score;

import com.onair.hearit.domain.Hearit;
import com.onair.hearit.infrastructure.BookmarkRepository;
import com.onair.hearit.infrastructure.CategoryBookmarkCount;
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

    private final BookmarkRepository bookmarkRepository;

    @Override
    public Map<Long, Double> calculate(Long memberId, List<Hearit> hearits) {
        Map<Long, Long> bookmarkCountsByCategory = getBookmarkCountsByCategory(memberId);
        long totalBookmarkCount = calculateTotalBookmarkCount(bookmarkCountsByCategory);

        return hearits.stream()
                .collect(Collectors.toMap(
                        Hearit::getId,
                        hearit -> calculateBookmarkScore(hearit, bookmarkCountsByCategory, totalBookmarkCount)
                ));
    }

    private Map<Long, Long> getBookmarkCountsByCategory(Long memberId) {
        return bookmarkRepository.countBookmarksByCategoryId(memberId).stream()
                .collect(Collectors.toMap(
                        CategoryBookmarkCount::getCategoryId,
                        CategoryBookmarkCount::getCount
                ));
    }

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
}
