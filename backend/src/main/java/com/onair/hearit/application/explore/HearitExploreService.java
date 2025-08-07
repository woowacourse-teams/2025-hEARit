package com.onair.hearit.application.explore;

import com.onair.hearit.application.explore.score.BookmarkScoreFactor;
import com.onair.hearit.application.explore.score.RandomScoreFactor;
import com.onair.hearit.application.explore.score.RecencyScoreFactor;
import com.onair.hearit.application.explore.score.ScoreFactor;
import com.onair.hearit.domain.Bookmark;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.dto.response.CursorResponse;
import com.onair.hearit.dto.response.ExploredHearitResponse;
import com.onair.hearit.infrastructure.BookmarkRepository;
import com.onair.hearit.infrastructure.ExploreScoreCommandRepository;
import com.onair.hearit.infrastructure.ExploredHearitQueryRepository;
import com.onair.hearit.infrastructure.HearitKeywordRepository;
import com.onair.hearit.infrastructure.HearitRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HearitExploreService {

    private static final int KEYWORDS_PER_HEARIT_FOR_RANDOM = 5;

    private final ExploreScoreCommandRepository exploreScoreCommandRepository;
    private final ExploredHearitQueryRepository exploredHearitQueryRepository;
    private final HearitRepository hearitRepository;
    private final HearitKeywordRepository hearitKeywordRepository;
    private final BookmarkRepository bookmarkRepository;

    private final ExploreScoreCalculator exploreScoreCalculator;
    private final BookmarkScoreFactor bookmarkScoreFactor;
    private final RecencyScoreFactor recencyScoreFactor;
    private final RandomScoreFactor randomScoreFactor;

    public CursorResponse<ExploredHearitResponse> getExploredHearits(Long memberId, Long cursorId, int size) {
        if (isFirstExploreRequest(cursorId)) {
            generateScores(memberId);
        }

        List<Hearit> exploredHearits = findExploredHearits(memberId, cursorId, size);
        List<ExploredHearitResponse> exploredHearitsDto = exploredHearits.stream()
                .map(hearit -> toExploredHearitResponse(hearit, memberId, cursorId))
                .toList();
        return CursorResponse.from(exploredHearitsDto);
    }

    private boolean isFirstExploreRequest(Long cursorId) {
        return cursorId == 0;
    }

    private void generateScores(Long memberId) {
        List<Hearit> hearits = hearitRepository.findAll();
        List<ScoreFactor> scoreFactors = determineScoreFactors(memberId);

        Map<Long, Double> scores = exploreScoreCalculator.calculateTotalScores(memberId, hearits, scoreFactors);
        exploreScoreCommandRepository.insertScores(memberId, scores);
        exploreScoreCommandRepository.updateCursorIds(memberId);
    }

    private List<ScoreFactor> determineScoreFactors(Long memberId) {
        if (memberId == null) {
            return List.of(recencyScoreFactor, randomScoreFactor);
        }
        return List.of(bookmarkScoreFactor, recencyScoreFactor, randomScoreFactor);
    }

    private List<Hearit> findExploredHearits(Long memberId, Long cursorId, int size) {
        if (memberId == null) {
            return exploredHearitQueryRepository.findExploredHearitsForGuest(cursorId, size);
        }
        return exploredHearitQueryRepository.findExploredHearits(memberId, cursorId, size);
    }

    private ExploredHearitResponse toExploredHearitResponse(Hearit hearit, Long memberId, Long cursorId) {
        List<Keyword> keywords = hearitKeywordRepository.findRecentKeywordsByHearitId(
                hearit.getId(), KEYWORDS_PER_HEARIT_FOR_RANDOM);

        Optional<Bookmark> bookmarkOptional = bookmarkRepository.findByHearitIdAndMemberId(
                hearit.getId(), memberId);
        if (bookmarkOptional.isPresent()) {
            return ExploredHearitResponse.fromWithBookmark(hearit, bookmarkOptional.get(), keywords, cursorId);
        }
        return ExploredHearitResponse.from(hearit, keywords, cursorId);
    }
}
