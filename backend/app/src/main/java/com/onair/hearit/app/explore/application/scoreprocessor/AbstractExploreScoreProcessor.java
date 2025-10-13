package com.onair.hearit.app.explore.application.scoreprocessor;

import com.onair.hearit.app.explore.application.ExploreScoreInitializer;
import com.onair.hearit.app.explore.dto.ExploredHearitResponse;
import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.HearitKeyword;
import com.onair.hearit.core.domain.Keyword;
import com.onair.hearit.core.domain.UserInfo;
import com.onair.hearit.core.infrastructure.jpa.ExploredHearitQueryRepository;
import com.onair.hearit.core.infrastructure.jpa.HearitKeywordRepository;
import com.onair.hearit.core.infrastructure.projection.ExploredHearitProjection;
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

    protected final HearitKeywordRepository hearitKeywordRepository;
    private final ExploreScoreInitializer exploreScoreInitializer;
    private final ExploredHearitQueryRepository exploredHearitQueryRepository;

    @Override
    public void refreshScores(UserInfo userInfo, long cursorId) {
        exploreScoreInitializer.refreshScores(cursorId, getUserUuid(userInfo), userInfo.getUserType());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExploredHearitResponse> getExploreHearits(UserInfo userInfo, long cursorId, int size) {
        String userUuid = getUserUuid(userInfo);
        List<ExploredHearitProjection> exploredHearitProjections =
                exploredHearitQueryRepository.findExploredHearits(userUuid, cursorId, Pageable.ofSize(size));
        if (exploredHearitProjections.isEmpty()) {
            return List.of();
        }
        return convertToExploredHearitResponses(exploredHearitProjections, userInfo);
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

    protected abstract String getUserUuid(UserInfo userInfo);

    protected abstract List<ExploredHearitResponse> convertToExploredHearitResponses(
            List<ExploredHearitProjection> infos,
            UserInfo userInfo
    );
}
