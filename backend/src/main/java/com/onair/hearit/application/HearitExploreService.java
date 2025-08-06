package com.onair.hearit.application;

import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.dto.response.ExploredHearitResponse;
import com.onair.hearit.infrastructure.BookmarkRepository;
import com.onair.hearit.infrastructure.HearitKeywordRepository;
import com.onair.hearit.infrastructure.MemberHearitScoreCommandRepository;
import com.onair.hearit.infrastructure.MemberHearitScoreQueryRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HearitExploreService {

    private static final int KEYWORDS_PER_HEARIT_FOR_RANDOM = 5;

    private final MemberHearitScoreCommandRepository memberHearitScoreCommandRepository;
    private final MemberHearitScoreQueryRepository memberHearitScoreQueryRepository;
    private final HearitKeywordRepository hearitKeywordRepository;
    private final BookmarkRepository bookmarkRepository;

    public List<ExploredHearitResponse> getExploredHearits(Long memberId, Long cursorId, int size) {
        if (memberId == null) {
            if (cursorId == 0) {
                memberHearitScoreCommandRepository.generateDefaultScore();
            }
            List<Hearit> exploredHearits = memberHearitScoreQueryRepository.findExploredHearitsForGuest(cursorId, size);
            return exploredHearits.stream()
                    .map(hearit -> toExploredHearitResponse(hearit, memberId, cursorId))
                    .toList();
        }

        if (cursorId == 0) {
            memberHearitScoreCommandRepository.generatePersonalScore(memberId);
        }

        List<Hearit> exploredHearits = memberHearitScoreQueryRepository.findExploredHearits(memberId, cursorId, size);
        return exploredHearits.stream()
                .map(hearit -> toExploredHearitResponse(hearit, memberId, cursorId))
                .toList();
    }

    private ExploredHearitResponse toExploredHearitResponse(Hearit hearit, Long memberId, Long cursorId) {
        List<Keyword> keywords = hearitKeywordRepository.findRecentKeywordsByHearitId(hearit.getId(),
                KEYWORDS_PER_HEARIT_FOR_RANDOM);
        Optional<Bookmark> bookmarkOptional = bookmarkRepository.findByHearitIdAndMemberId(hearit.getId(), memberId);
        if (bookmarkOptional.isPresent()) {
            return ExploredHearitResponse.fromWithBookmark(hearit, bookmarkOptional.get(), keywords, cursorId);
        }
        return ExploredHearitResponse.from(hearit, keywords, cursorId);
    }
}
