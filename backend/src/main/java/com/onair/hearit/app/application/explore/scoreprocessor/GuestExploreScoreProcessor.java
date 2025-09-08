package com.onair.hearit.app.application.explore.scoreprocessor;

import com.onair.hearit.app.application.explore.ExploreScoreCalculator;
import com.onair.hearit.app.dto.response.ExploredHearitResponse;
import com.onair.hearit.auth.domain.UserContext;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.Keyword;
import com.onair.hearit.common.infrastructure.jdbc.ExploreScoreCommandRepository;
import com.onair.hearit.common.infrastructure.jpa.ExploredHearitQueryRepository;
import com.onair.hearit.common.infrastructure.jpa.HearitKeywordRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GuestExploreScoreProcessor implements ExploreScoreProcessor {

    private static final int KEYWORDS_PER_HEARIT_FOR_RANDOM = 5;

    private final ExploreScoreCalculator exploreScoreCalculator;
    private final ExploreScoreCommandRepository exploreScoreCommandRepository;
    private final ExploredHearitQueryRepository exploredHearitQueryRepository;
    private final HearitKeywordRepository hearitKeywordRepository;

    @Override
    public boolean isSupported(UserContext userContext) {
        if (userContext == null || userContext.isGuest()) {
            return true;
        }
        return false;
    }

    @Override
    public List<ExploredHearitResponse> getExploreHearitsResponse(UserContext userContext, long cursorId, int size) {
        List<Hearit> exploredHearits = getExploredHearits(userContext, cursorId, size);
        return exploredHearits.stream()
                .map(this::toExploredHearitResponse)
                .toList();
    }

    private List<Hearit> getExploredHearits(UserContext userContext, Long cursorId, int size) {
        String uuid = userContext.getGuestId();

        if (cursorId == 0L) {
            Map<Long, Double> scores = exploreScoreCalculator.calculateTotalScores(uuid, userContext.getUserType());
            exploreScoreCommandRepository.insertScores(uuid, scores);
            exploreScoreCommandRepository.updateCursorIds(uuid);
        }
        return exploredHearitQueryRepository.findExploredHearits(uuid, cursorId, size);
    }

    private ExploredHearitResponse toExploredHearitResponse(Hearit hearit) {
        List<Keyword> keywords = hearitKeywordRepository.findRecentKeywordsByHearitId(
                hearit.getId(), KEYWORDS_PER_HEARIT_FOR_RANDOM);
        return ExploredHearitResponse.from(hearit, keywords);
    }
}
