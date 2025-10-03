package com.onair.hearit.app.explore.application.scoreprocessor;

import com.onair.hearit.explore.dto.ExploredHearitResponse;
import com.onair.hearit.domain.UserInfo;
import java.util.List;

public interface ExploreScoreProcessor {

    boolean isSupported(UserInfo userInfo);

    void refreshScoresIfNeeded(UserInfo userInfo, long cursorId);

    List<ExploredHearitResponse> getExploreHearits(UserInfo userInfo, long cursorId, int size);
}
