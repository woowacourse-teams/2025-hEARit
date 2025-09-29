package com.onair.hearit.explore.application.scoreprocessor;

import com.onair.hearit.domain.UserInfo;
import com.onair.hearit.explore.dto.ExploredHearitResponse;
import java.util.List;

public interface ExploreScoreProcessor {

    boolean isSupported(UserInfo userInfo);

    String resolveUserUuid(UserInfo userInfo);

    List<ExploredHearitResponse> getExploreHearitsResponse(UserInfo userInfo, String userUuid,
                                                           long cursorId, int size);
}
