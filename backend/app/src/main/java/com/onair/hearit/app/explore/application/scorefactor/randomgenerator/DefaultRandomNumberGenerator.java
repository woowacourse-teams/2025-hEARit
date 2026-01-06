package com.onair.hearit.app.explore.application.scorefactor.randomgenerator;

import java.util.Random;
import org.springframework.stereotype.Component;

@Component
public class DefaultRandomNumberGenerator implements RandomNumberGenerator {

    private final Random random = new Random();

    @Override
    public double getDouble() {
        return random.nextDouble();
    }
}
