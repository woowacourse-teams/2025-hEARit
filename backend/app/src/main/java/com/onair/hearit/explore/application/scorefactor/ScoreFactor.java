package com.onair.hearit.explore.application.scorefactor;

import com.onair.hearit.domain.Hearit;
import com.onair.hearit.domain.UserType;
import java.util.List;
import java.util.Map;

public interface ScoreFactor {

    Map<Long, Double> calculate(String uuid, List<Hearit> hearits);

    boolean isSupported(UserType userType);
}
