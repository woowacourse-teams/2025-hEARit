package com.onair.hearit.core.log.mask.strategy;

import com.onair.hearit.core.log.mask.MaskingType;
import org.springframework.stereotype.Component;

@Component
public class FullMaskingStrategy implements MaskingStrategy {

    @Override
    public MaskingType type() {
        return MaskingType.FULL;
    }

    @Override
    public String mask(String value) {
        return "*****";
    }
}
