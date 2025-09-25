package com.onair.hearit.explore.application.scoreprocessor;

import com.onair.hearit.explore.application.ExploreScoreCalculator;
import com.onair.hearit.explore.dto.ExploredHearitResponse;
import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.HearitKeyword;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.domain.UserInfo;
import com.onair.hearit.infrastructure.projection.ExploredHearitProjection;
import com.onair.hearit.infrastructure.jdbc.ExploreScoreCommandRepository;
import com.onair.hearit.infrastructure.jpa.ExploredHearitQueryRepository;
import com.onair.hearit.infrastructure.jpa.HearitKeywordRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractExploreScoreProcessor implements ExploreScoreProcessor {

    protected static final int KEYWORDS_PER_HEARIT_FOR_RANDOM = 5;

    private final ExploreScoreCalculator exploreScoreCalculator;
    private final ExploreScoreCommandRepository exploreScoreCommandRepository;
    private final ExploredHearitQueryRepository exploredHearitQueryRepository;
    protected final HearitKeywordRepository hearitKeywordRepository;

    @Override
    @Transactional
    public List<ExploredHearitResponse> getExploreHearitsResponse(UserInfo userInfo, long cursorId, int size) {
        List<ExploredHearitProjection> exploredHearitProjections = getExploredHearits(userInfo, cursorId, size);
        if (exploredHearitProjections.isEmpty()) {
            return List.of();
        }
        return convertToExploredHearitResponses(exploredHearitProjections, userInfo);
    }

    private List<ExploredHearitProjection> getExploredHearits(UserInfo userInfo, Long cursorId, int size) {
        String userId = getUserUuId(userInfo);

        if (cursorId == 0L) {
            Map<Long, Double> scores = exploreScoreCalculator.calculateTotalScores(userId, userInfo.getUserType());
            exploreScoreCommandRepository.insertScores(userId, scores);
            exploreScoreCommandRepository.updateCursorIds(userId);
        }
        return exploredHearitQueryRepository.findExploredHearits(userId, cursorId, Pageable.ofSize(size));
    }

    protected Map<Hearit, List<Keyword>> prepareKeywordsMap(List<Hearit> hearits) {
        Map<Hearit, List<HearitKeyword>> allHearitKeywordsMap = hearitKeywordRepository.findAllByHearitIn(hearits)
                .stream()
                .collect(Collectors.groupingBy(HearitKeyword::getHearit));

        return allHearitKeywordsMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .sorted((hk1, hk2) -> hk2.getId().compareTo(hk1.getId()))
                                .limit(KEYWORDS_PER_HEARIT_FOR_RANDOM)
                                .map(HearitKeyword::getKeyword)
                                .toList()
                ));
    }

    protected abstract String getUserUuId(UserInfo userInfo);

    protected abstract List<ExploredHearitResponse> convertToExploredHearitResponses(
            List<ExploredHearitProjection> infos,
            UserInfo userInfo
    );
}
