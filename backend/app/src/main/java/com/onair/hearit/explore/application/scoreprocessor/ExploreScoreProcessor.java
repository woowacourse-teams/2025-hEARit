package com.onair.hearit.explore.application.scoreprocessor;

import com.onair.hearit.domain.UserInfo;
import com.onair.hearit.explore.dto.ExploredHearitResponse;
import java.util.List;

public interface ExploreScoreProcessor {

    boolean isSupported(UserInfo userInfo);

    void refreshScoresIfNeeded(UserInfo userInfo, long cursorId);

    List<ExploredHearitResponse> fetchExploreHearits(UserInfo userInfo, long cursorId, int size);

    default List<ExploredHearitResponse> getExploreHearitsResponse(UserInfo userInfo, long cursorId, int size) {
        refreshScoresIfNeeded(userInfo, cursorId);
        return fetchExploreHearits(userInfo, cursorId, size);
    }
}
