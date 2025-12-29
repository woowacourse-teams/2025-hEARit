package com.onair.hearit.app.explore.application.scorefactor;

import com.onair.hearit.core.domain.Hearit;
import com.onair.hearit.core.domain.UserType;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RandomScoreFactor implements ScoreFactor {

    private static final int MAX_RANDOM_SCORE = 10;

    private final RandomNumberGenerator randomNumberGenerator;

    @Override
    public boolean isSupported(UserType userType) {
        return userType == UserType.GUEST || userType == UserType.MEMBER;
    }

    @Override
    public Map<Long, Double> calculate(UUID uuid, List<Hearit> hearits) {
        return hearits.stream()
                .collect(Collectors.toMap(
                        Hearit::getId,
                        h -> randomNumberGenerator.getDouble() * MAX_RANDOM_SCORE
                ));
    }
}
