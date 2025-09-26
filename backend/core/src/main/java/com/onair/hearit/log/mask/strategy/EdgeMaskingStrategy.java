
package com.onair.hearit.log.mask.strategy;

import com.onair.hearit.log.mask.MaskingType;
import org.springframework.stereotype.Component;

@Component
public class EdgeMaskingStrategy implements MaskingStrategy {

    @Override
    public MaskingType type() {
        return MaskingType.EDGE;
    }

    @Override
    public String mask(String value) {
        if (value == null || value.length() <= 2) {
            return "***";
        }
        return value.charAt(0) + "*".repeat(value.length() - 2) + value.charAt(value.length() - 1);
    }
}
