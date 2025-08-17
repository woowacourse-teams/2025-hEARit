package com.onair.hearit.log.mask.strategy;

import com.onair.hearit.log.mask.MaskingType;

public interface MaskingStrategy {

    MaskingType type();

    String mask(String value);
}
