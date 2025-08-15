package com.onair.hearit.common.log.mask.strategy;

import com.onair.hearit.common.log.mask.MaskingType;
import org.springframework.stereotype.Component;

@Component
public class TokenMaskingStrategy implements MaskingStrategy {

    private static final int PREFIX_VISIBLE_LENGTH = 5; //토큰 유형, 발급처 또는 서명 방식 파악용
    private static final int SUFFIX_VISIBLE_LENGTH = 4; //고유 식별자 뒷부분 식별용

    @Override
    public MaskingType type() {
        return MaskingType.TOKEN;
    }

    @Override
    public String mask(String input) {
        if (input.length() <= 10) {
            return "*****";
        }
        return input.substring(0, PREFIX_VISIBLE_LENGTH) + "..." + input.substring(input.length() - SUFFIX_VISIBLE_LENGTH);
    }
}
