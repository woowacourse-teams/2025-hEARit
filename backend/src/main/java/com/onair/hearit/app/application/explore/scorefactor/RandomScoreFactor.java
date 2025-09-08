package com.onair.hearit.app.application.explore.scorefactor;

import com.onair.hearit.auth.domain.UserType;
import com.onair.hearit.common.domain.Hearit;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RandomScoreFactor implements ScoreFactor {

    private static final int MAX_RANDOM_SCORE = 10;

    private final Random random = new Random();

    @Override
    public boolean isSupported(UserType userType) {
        return userType == UserType.GUEST || userType == UserType.MEMBER;
    }

    @Override
    public Map<Long, Double> calculate(String uuid, List<Hearit> hearits) {
        return hearits.stream()
                .collect(Collectors.toMap(
                        Hearit::getId,
                        h -> random.nextDouble() * MAX_RANDOM_SCORE
                ));
    }
}
