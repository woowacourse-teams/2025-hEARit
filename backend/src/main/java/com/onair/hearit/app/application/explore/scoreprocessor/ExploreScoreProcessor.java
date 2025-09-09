package com.onair.hearit.app.application.explore.scoreprocessor;

import com.onair.hearit.app.dto.response.ExploredHearitResponse;
import com.onair.hearit.common.domain.UserInfo;
import java.util.List;

public interface ExploreScoreProcessor {

    boolean isSupported(UserInfo userInfo);

    List<ExploredHearitResponse> getExploreHearitsResponse(UserInfo userInfo, long cursorId, int size);
}
