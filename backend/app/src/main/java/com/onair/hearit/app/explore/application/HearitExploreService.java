package com.onair.hearit.app.explore.application;

import com.onair.hearit.app.explore.application.scoreprocessor.ExploreScoreProcessor;
import com.onair.hearit.app.explore.dto.CursorRequest;
import com.onair.hearit.app.explore.dto.CursorResponseV2;
import com.onair.hearit.app.explore.dto.ExploreCursor;
import com.onair.hearit.app.explore.dto.ExploredHearitResponse;
import com.onair.hearit.app.explore.dto.ExploredHearitResponseV3;
import com.onair.hearit.core.domain.UserInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HearitExploreService {

    private final List<ExploreScoreProcessor> exploreScoreProcessors;

    //TODO v3 api 로 전환 후 제거
    @Transactional
    public CursorResponseV2<ExploredHearitResponse> getExploredHearits(UserInfo userInfo,
                                                                       CursorRequest cursorRequest) {
        ExploreScoreProcessor exploreScoreProcessor = getExploreScoreProcessor(userInfo);
        exploreScoreProcessor.refreshScores(userInfo, cursorRequest.cursorId());
        List<ExploredHearitResponse> exploreHearitsResponses = exploreScoreProcessor.getExploreHearits(
                userInfo, cursorRequest.cursorId(), cursorRequest.size());
        return CursorResponseV2.from(exploreHearitsResponses);
    }

    @Transactional
    public CursorResponseV2<ExploredHearitResponseV3> getExploredHearitsV3(UserInfo userInfo,
                                                                            String cursor,
                                                                            int size) {
        ExploreScoreProcessor processor = getExploreScoreProcessor(userInfo);
        ExploreCursor exploreCursor = ExploreCursor.from(cursor);
        if (exploreCursor.isInitial()) {
            processor.refreshScoresV3(userInfo);
        }
        List<ExploredHearitResponseV3> responses = processor.getExploreHearitsV3(userInfo, exploreCursor, size);
        return CursorResponseV2.from(responses);
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
