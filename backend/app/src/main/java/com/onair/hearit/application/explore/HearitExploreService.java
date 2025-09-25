package com.onair.hearit.application.explore;

import com.onair.hearit.application.explore.scoreprocessor.ExploreScoreProcessor;
import com.onair.hearit.dto.request.CursorRequest;
import com.onair.hearit.dto.response.CursorResponseV2;
import com.onair.hearit.dto.response.ExploredHearitResponse;
import com.onair.hearit.domain.UserInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HearitExploreService {

    private final List<ExploreScoreProcessor> exploreScoreProcessors;

    public CursorResponseV2<ExploredHearitResponse> getExploredHearits(UserInfo userInfo,
                                                                       CursorRequest cursorRequest) {
        ExploreScoreProcessor exploreScoreProcessor = getExploreScoreProcessor(userInfo);
        List<ExploredHearitResponse> exploreHearitsResponses =
                exploreScoreProcessor.getExploreHearitsResponse(
                        userInfo,
                        cursorRequest.cursorId(),
                        cursorRequest.size());
        return CursorResponseV2.from(exploreHearitsResponses);
    }

    private ExploreScoreProcessor getExploreScoreProcessor(UserInfo userInfo) {
        for (ExploreScoreProcessor exploreScoreProcessor : exploreScoreProcessors) {
            if (exploreScoreProcessor.isSupported(userInfo)) {
                return exploreScoreProcessor;
            }
        }
        //TODO: 커스텀예외
        throw new IllegalStateException("지원하지 않는 유저입니다.");
    }
}
