package com.onair.hearit.app.application.explore.scoreprocessor;

import com.onair.hearit.app.application.explore.ExploreScoreCalculator;
import com.onair.hearit.app.dto.response.ExploredHearitResponse;
import com.onair.hearit.common.domain.Hearit;
import com.onair.hearit.common.domain.Keyword;
import com.onair.hearit.common.domain.UserInfo;
import com.onair.hearit.common.infrastructure.dto.ExploredHearitProjection;
import com.onair.hearit.common.infrastructure.jdbc.ExploreScoreCommandRepository;
import com.onair.hearit.common.infrastructure.jpa.ExploredHearitQueryRepository;
import com.onair.hearit.common.infrastructure.jpa.HearitKeywordRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GuestExploreScoreProcessor extends AbstractExploreScoreProcessor {

    public GuestExploreScoreProcessor(ExploreScoreCalculator exploreScoreCalculator,
                                      ExploreScoreCommandRepository exploreScoreCommandRepository,
                                      ExploredHearitQueryRepository exploredHearitQueryRepository,
                                      HearitKeywordRepository hearitKeywordRepository) {
        super(exploreScoreCalculator, exploreScoreCommandRepository,
                exploredHearitQueryRepository, hearitKeywordRepository);
    }

    @Override
    public boolean isSupported(UserInfo userInfo) {
        return userInfo != null && userInfo.isGuest();
    }

    @Override
    protected String getUserUuId(UserInfo userInfo) {
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
