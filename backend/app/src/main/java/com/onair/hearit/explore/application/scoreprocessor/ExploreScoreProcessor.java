package com.onair.hearit.explore.application.scoreprocessor;

import com.onair.hearit.domain.UserInfo;
import com.onair.hearit.explore.dto.ExploredHearitResponse;
import java.util.List;

public interface ExploreScoreProcessor {

    boolean isSupported(UserInfo userInfo);

    String resolveUserUuid(UserInfo userInfo);

    void refreshScoresIfNeeded(long cursorId, UserInfo userInfo, String userUuid);

    List<ExploredHearitResponse> fetchExploreHearits(String userUuid, long cursorId, int size, UserInfo userInfo);

    default List<ExploredHearitResponse> getExploreHearitsResponse(UserInfo userInfo, long cursorId, int size) {
        String userUuid = resolveUserUuid(userInfo);
        refreshScoresIfNeeded(cursorId, userInfo, userUuid);
        return fetchExploreHearits(userUuid, cursorId, size, userInfo);
    }
}
