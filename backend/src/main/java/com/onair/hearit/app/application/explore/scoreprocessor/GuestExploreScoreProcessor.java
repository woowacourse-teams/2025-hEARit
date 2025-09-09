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
    public boolean isSupported(UserContext userContext) {
        return userContext == null || userContext.isGuest();
    }

    @Override
    protected String getUserUuId(UserContext userContext) {
        return userContext.getGuestId();
    }

    @Override
    protected ExploredHearitResponse toExploredHearitResponse(Hearit hearit, UserContext userContext) {
        List<Keyword> keywords = getKeywords(hearit); // 부모 클래스의 공통 메소드 사용
        return ExploredHearitResponse.from(hearit, keywords);
    }
}
