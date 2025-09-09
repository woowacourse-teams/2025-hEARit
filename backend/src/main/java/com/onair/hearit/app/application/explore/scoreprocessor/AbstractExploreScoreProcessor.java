package com.onair.hearit.app.application.explore.scoreprocessor;

import com.onair.hearit.app.application.explore.ExploreScoreCalculator;
import com.onair.hearit.app.dto.response.ExploredHearitResponse;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.Keyword;
import com.onair.hearit.common.domain.UserInfo;
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
    public List<ExploredHearitResponse> getExploreHearitsResponse(UserInfo userInfo, long cursorId, int size) {
        List<ExploredHearitInfo> exploredHearitInfos = getExploredHearits(userInfo, cursorId, size);
        return exploredHearitInfos.stream()
                .map(info -> toExploredHearitResponse(info, userInfo))
                .toList();
    }

    private List<ExploredHearitInfo> getExploredHearits(UserInfo userInfo, Long cursorId, int size) {
        String userId = getUserUuId(userInfo);

        if (cursorId == 0L) {
            Map<Long, Double> scores = exploreScoreCalculator.calculateTotalScores(userId, userInfo.getUserType());
            exploreScoreCommandRepository.insertScores(userId, scores);
            exploreScoreCommandRepository.updateCursorIds(userId);
        }
        return exploredHearitQueryRepository.findExploredHearits(userId, cursorId, Pageable.ofSize(size));
    }

    protected List<Keyword> getKeywords(Hearit hearit) {
        return hearitKeywordRepository.findRecentKeywordsByHearitId(
                hearit.getId(), KEYWORDS_PER_HEARIT_FOR_RANDOM);
    }

    protected abstract String getUserUuId(UserInfo userInfo);

    protected abstract ExploredHearitResponse toExploredHearitResponse(ExploredHearitInfo exploredHearitInfo,
                                                                       UserInfo userInfo);
}
