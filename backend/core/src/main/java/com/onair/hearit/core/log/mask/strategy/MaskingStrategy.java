package com.onair.hearit.core.log.mask.strategy;

import com.onair.hearit.core.log.mask.MaskingType;

public interface MaskingStrategy {

    MaskingType type();

    String mask(String value);
}
