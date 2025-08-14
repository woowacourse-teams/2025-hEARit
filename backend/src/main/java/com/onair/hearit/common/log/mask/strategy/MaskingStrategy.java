package com.onair.hearit.common.log.mask.strategy;

import com.onair.hearit.common.log.mask.MaskingType;

public interface MaskingStrategy {

    MaskingType type();

    String mask(String value);
}
