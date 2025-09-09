package com.onair.hearit.app.application.explore.scoreprocessor;

import com.onair.hearit.app.application.explore.ExploreScoreCalculator;
import com.onair.hearit.app.dto.response.ExploredHearitResponse;
import com.onair.hearit.auth.domain.UserContext;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.Keyword;
import com.onair.hearit.common.infrastructure.dto.ExploredHearitInfo;
import com.onair.hearit.common.infrastructure.jdbc.ExploreScoreCommandRepository;
import com.onair.hearit.common.infrastructure.jpa.ExploredHearitQueryRepository;
import com.onair.hearit.common.infrastructure.jpa.HearitKeywordRepository;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractExploreScoreProcessor implements ExploreScoreProcessor {

    private static final int KEYWORDS_PER_HEARIT_FOR_RANDOM = 5;

    private final ExploreScoreCalculator exploreScoreCalculator;
    private final ExploreScoreCommandRepository exploreScoreCommandRepository;
    private final ExploredHearitQueryRepository exploredHearitQueryRepository;
    private final HearitKeywordRepository hearitKeywordRepository;

    @Override
    public List<ExploredHearitResponse> getExploreHearitsResponse(UserContext userContext, long cursorId, int size) {
        List<ExploredHearitInfo> exploredHearitInfos = getExploredHearits(userContext, cursorId, size);
        return exploredHearitInfos.stream()
                .map(info -> toExploredHearitResponse(info, userContext))
                .toList();
    }

    private List<ExploredHearitInfo> getExploredHearits(UserContext userContext, Long cursorId, int size) {
        String userId = getUserUuId(userContext);

        if (cursorId == 0L) {
            Map<Long, Double> scores = exploreScoreCalculator.calculateTotalScores(userId, userContext.getUserType());
            exploreScoreCommandRepository.insertScores(userId, scores);
            exploreScoreCommandRepository.updateCursorIds(userId);
        }
        return exploredHearitQueryRepository.findExploredHearits(userId, cursorId, Pageable.ofSize(size));
    }

    protected List<Keyword> getKeywords(Hearit hearit) {
        return hearitKeywordRepository.findRecentKeywordsByHearitId(
                hearit.getId(), KEYWORDS_PER_HEARIT_FOR_RANDOM);
    }

    protected abstract String getUserUuId(UserContext userContext);
    protected abstract ExploredHearitResponse toExploredHearitResponse(ExploredHearitInfo exploredHearitInfo, UserContext userContext);
}
