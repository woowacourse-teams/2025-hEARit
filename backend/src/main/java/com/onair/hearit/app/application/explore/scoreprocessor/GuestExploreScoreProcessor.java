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
            List<ExploredHearitInfo> exploredHearitInfos,
            UserInfo userInfo) {
        List<Hearit> hearits = exploredHearitInfos.stream()
                .map(ExploredHearitInfo::getHearit)
                .toList();
        Map<Hearit, List<Keyword>> keywordsMap = prepareKeywordsMap(hearits);

        return exploredHearitInfos.stream()
                .map(info -> {
                    List<Keyword> keywords = keywordsMap.getOrDefault(info.getHearit(), List.of());
                    return ExploredHearitResponse.from(info.getHearit(), keywords, info.getCursorId());
                })
                .toList();
    }
}
