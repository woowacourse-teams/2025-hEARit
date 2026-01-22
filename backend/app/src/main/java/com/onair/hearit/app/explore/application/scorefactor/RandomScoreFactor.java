package com.onair.hearit.app.explore.application.scorefactor;

import com.onair.hearit.app.explore.application.scorefactor.generator.RandomNumberGenerator;
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
                        h -> getRandomValue()
                ));
    }

    private double getRandomValue() {
        double value = randomNumberGenerator.getDouble();
        // 사용자가 다양한 컨텐츠를 접하기 위해 10% 확률로 최대 점수 부여
        if (value <= 0.1) {
            return 1.0;
        }
        return value;
    }
}
