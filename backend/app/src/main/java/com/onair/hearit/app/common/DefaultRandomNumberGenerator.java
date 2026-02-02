package com.onair.hearit.app.common;

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
