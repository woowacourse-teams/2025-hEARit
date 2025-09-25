package com.onair.hearit.application.explore.scoreprocessor;

import com.onair.hearit.dto.response.ExploredHearitResponse;
import com.onair.hearit.domain.UserInfo;
import java.util.List;

public interface ExploreScoreProcessor {

    boolean isSupported(UserInfo userInfo);

    List<ExploredHearitResponse> getExploreHearitsResponse(UserInfo userInfo, long cursorId, int size);
}
