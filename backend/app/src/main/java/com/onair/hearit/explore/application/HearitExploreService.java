package com.onair.hearit.explore.application;

import com.onair.hearit.common.dto.request.CursorRequest;
import com.onair.hearit.common.dto.response.CursorResponseV2;
import com.onair.hearit.domain.UserInfo;
import com.onair.hearit.explore.application.scoreprocessor.ExploreScoreProcessor;
import com.onair.hearit.explore.dto.ExploredHearitResponse;
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
        String userUuid = exploreScoreProcessor.resolveUserUuid(userInfo);
        exploreScoreProcessor.refreshScoresIfNeeded(cursorRequest.cursorId(), userInfo, userUuid);
        List<ExploredHearitResponse> exploreHearitsResponses = exploreScoreProcessor.fetchExploreHearits(
                userUuid, cursorRequest.cursorId(), cursorRequest.size(), userInfo);
        return CursorResponseV2.from(exploreHearitsResponses);
    }

    private ExploreScoreProcessor getExploreScoreProcessor(UserInfo userInfo) {
        for (ExploreScoreProcessor exploreScoreProcessor : exploreScoreProcessors) {
            if (exploreScoreProcessor.isSupported(userInfo)) {
                return exploreScoreProcessor;
            }
        }
        //TODO: 예외 처리
        throw new IllegalStateException("지원하지 않는 탐색 요청입니다.");
    }
}
