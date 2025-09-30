package com.onair.hearit.explore.application.scoreprocessor;

import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.HearitKeyword;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.domain.UserInfo;
import com.onair.hearit.explore.application.ExploreScoreRefresher;
import com.onair.hearit.explore.dto.ExploredHearitResponse;
import com.onair.hearit.infrastructure.jpa.ExploredHearitQueryRepository;
import com.onair.hearit.infrastructure.jpa.HearitKeywordRepository;
import com.onair.hearit.infrastructure.projection.ExploredHearitProjection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractExploreScoreProcessor implements ExploreScoreProcessor {

    protected static final int KEYWORDS_PER_HEARIT_FOR_RANDOM = 5;

    private final ExploreScoreRefresher exploreScoreRefresher;
    private final ExploredHearitQueryRepository exploredHearitQueryRepository;
    protected final HearitKeywordRepository hearitKeywordRepository;

    @Override
    public final List<ExploredHearitResponse> getExploreHearitsResponse(UserInfo userInfo, long cursorId,
                                                                        int size) {
        String userUuid = getUserUuid(userInfo);
        exploreScoreRefresher.refreshIfNeeded(cursorId, userUuid, userInfo.getUserType());

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
