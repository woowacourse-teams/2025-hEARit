package com.onair.hearit.app.application.explore.scoreprocessor;

import com.onair.hearit.app.dto.response.ExploredHearitResponse;
import com.onair.hearit.auth.domain.UserContext;
import java.util.List;

public interface ExploreScoreProcessor {

    boolean isSupported(UserContext userContext);

    List<ExploredHearitResponse> getExploreHearitsResponse(UserContext userContext, long cursorId, int size);
}
