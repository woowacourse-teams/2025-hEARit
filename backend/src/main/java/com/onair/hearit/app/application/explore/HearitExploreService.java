package com.onair.hearit.app.application.explore;

import com.onair.hearit.app.application.explore.scoreprocessor.ExploreScoreProcessor;
import com.onair.hearit.app.dto.request.CursorRequest;
import com.onair.hearit.app.dto.response.CursorResponse;
import com.onair.hearit.app.dto.response.ExploredHearitResponse;
import com.onair.hearit.auth.domain.UserContext;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HearitExploreService {

    private final List<ExploreScoreProcessor> exploreScoreProcessors;

    public CursorResponse<ExploredHearitResponse> getExploredHearits(UserContext userContext,
                                                                     CursorRequest cursorRequest) {
        ExploreScoreProcessor exploreScoreProcessor = getExploreScoreProcessor(userContext);
        List<ExploredHearitResponse> exploreHearitsResponses =
                exploreScoreProcessor.getExploreHearitsResponse(
                        userContext,
                        cursorRequest.cursorId(),
                        cursorRequest.size());
        long updatedCursorId = cursorRequest.cursorId() + exploreHearitsResponses.size();
        return CursorResponse.from(exploreHearitsResponses, updatedCursorId);
    }

    private ExploreScoreProcessor getExploreScoreProcessor(UserContext userContext) {
        for (ExploreScoreProcessor exploreScoreProcessor : exploreScoreProcessors) {
            if (exploreScoreProcessor.isSupported(userContext)) {
                return exploreScoreProcessor;
            }
        }
        //FIXME : 커스텀예외
        throw new UnsupportedOperationException("지원하지 않는 유저입니다.");
    }
}
