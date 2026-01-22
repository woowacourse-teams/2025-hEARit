package com.onair.hearit.app.explore.application.scorefactor;

import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.UserType;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ScoreFactor {

    Map<Long, Double> calculate(UUID uuid, List<Hearit> hearits);

    boolean isSupported(UserType userType);
}
