package com.onair.hearit.explore.application.scoreprocessor;

import com.onair.hearit.explore.dto.ExploredHearitResponse;
import com.onair.hearit.domain.UserInfo;
import java.util.List;

public interface ExploreScoreProcessor {

    boolean isSupported(UserInfo userInfo);

    List<ExploredHearitResponse> getExploreHearitsResponse(UserInfo userInfo, long cursorId, int size);
}
