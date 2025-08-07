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

    private final BookmarkRepository bookmarkRepository;

    @Override
    public Map<Long, Double> calculate(Long memberId, List<Hearit> hearits) {
        Map<Long, Long> bookmarkCountsByCategory = bookmarkRepository.countBookmarksByCategoryId(memberId).stream()
                .collect(Collectors.toMap(
                        CategoryBookmarkCount::getCategoryId,
                        CategoryBookmarkCount::getCount
                ));

        long totalCount = Math.max(1, bookmarkCountsByCategory.values().stream()
                .mapToLong(Long::longValue)
                .sum());

        Map<Long, Double> collect = hearits.stream()
                .collect(Collectors.toMap(
                        Hearit::getId,
                        hearit -> {
                            long sameCategoryCount = bookmarkCountsByCategory.getOrDefault(hearit.getCategory().getId(),
                                    0L);
                            return Math.min(30.0, ((double) sameCategoryCount / totalCount) * 30.0);
                        }
                ));
        return collect;
    }
}
