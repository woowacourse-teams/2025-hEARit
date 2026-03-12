package com.onair.hearit.app.explore.application.scoreprocessor;

import com.onair.hearit.app.explore.dto.ExploreCursor;
import com.onair.hearit.app.explore.dto.ExploredHearitResponse;
import com.onair.hearit.app.explore.dto.ExploredHearitResponseV3;
import com.onair.hearit.core.domain.UserInfo;
import java.util.List;

public interface ExploreScoreProcessor {

    boolean isSupported(UserInfo userInfo);

    void refreshScores(UserInfo userInfo, long cursorId);

    List<ExploredHearitResponse> getExploreHearits(UserInfo userInfo, long cursorId, int size);

    void refreshScores(UserInfo userInfo);

    List<ExploredHearitResponseV3> getExploreHearits(UserInfo userInfo, ExploreCursor cursor, int size);
}
