package com.onair.hearit.explore.application.scoreprocessor;

import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.Keyword;
import com.onair.hearit.domain.UserInfo;
import com.onair.hearit.explore.application.ExploreScoreRefresher;
import com.onair.hearit.explore.dto.ExploredHearitResponse;
import com.onair.hearit.infrastructure.jpa.ExploredHearitQueryRepository;
import com.onair.hearit.infrastructure.jpa.HearitKeywordRepository;
import com.onair.hearit.infrastructure.projection.ExploredHearitProjection;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GuestExploreScoreProcessor extends AbstractExploreScoreProcessor {

    public GuestExploreScoreProcessor(ExploreScoreRefresher exploreScoreRefresher,
                                      ExploredHearitQueryRepository exploredHearitQueryRepository,
                                      HearitKeywordRepository hearitKeywordRepository) {
        super(exploreScoreRefresher, exploredHearitQueryRepository, hearitKeywordRepository);
    }

    @Override
    public boolean isSupported(UserInfo userInfo) {
        return userInfo != null && userInfo.isGuest();
    }

    @Override
    protected String getUserUuid(UserInfo userInfo) {
        return userInfo.getGuestId();
    }

    @Override
    protected List<ExploredHearitResponse> convertToExploredHearitResponses(
            List<ExploredHearitProjection> exploredHearitProjections,
            UserInfo userInfo) {
        List<Hearit> hearits = exploredHearitProjections.stream()
                .map(ExploredHearitProjection::getHearit)
                .toList();
        Map<Hearit, List<Keyword>> keywordsMap = prepareKeywordsMap(hearits);

        return exploredHearitProjections.stream()
                .map(info -> {
                    List<Keyword> keywords = keywordsMap.getOrDefault(info.getHearit(), List.of());
                    return ExploredHearitResponse.from(info.getHearit(), keywords, info.getCursorId());
                })
                .toList();
    }
}
