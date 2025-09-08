package com.onair.hearit.app.application.explore.scorefactor;

import com.onair.hearit.auth.domain.UserType;
import com.onair.hearit.common.domain.Hearit;
import java.util.List;
import java.util.Map;

public interface ScoreFactor {

    Map<Long, Double> calculate(String uuid, List<Hearit> hearits);

    boolean isSupported(UserType userType);
}
