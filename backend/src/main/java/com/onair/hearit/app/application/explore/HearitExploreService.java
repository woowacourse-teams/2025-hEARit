package com.onair.hearit.app.application.explore;

import com.onair.hearit.app.application.explore.scoreprocessor.ExploreScoreProcessor;
import com.onair.hearit.app.dto.request.CursorRequest;
import com.onair.hearit.app.dto.response.CursorResponse;
import com.onair.hearit.app.dto.response.ExploredHearitResponse;
import com.onair.hearit.common.domain.UserInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HearitExploreService {

    private final List<ExploreScoreProcessor> exploreScoreProcessors;

    public CursorResponse<ExploredHearitResponse> getExploredHearits(UserInfo userInfo,
                                                                     CursorRequest cursorRequest) {
        ExploreScoreProcessor exploreScoreProcessor = getExploreScoreProcessor(userInfo);
        List<ExploredHearitResponse> exploreHearitsResponses =
                exploreScoreProcessor.getExploreHearitsResponse(
                        userInfo,
                        cursorRequest.cursorId(),
                        cursorRequest.size());
        return CursorResponse.from(exploreHearitsResponses);
    }

    private ExploreScoreProcessor getExploreScoreProcessor(UserInfo userInfo) {
        for (ExploreScoreProcessor exploreScoreProcessor : exploreScoreProcessors) {
            if (exploreScoreProcessor.isSupported(userInfo)) {
                return exploreScoreProcessor;
            }
        }
        //FIXME : 커스텀예외
        throw new UnsupportedOperationException("지원하지 않는 유저입니다.");
    }
}
