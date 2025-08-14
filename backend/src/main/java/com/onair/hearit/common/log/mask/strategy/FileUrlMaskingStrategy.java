package com.onair.hearit.common.log.mask.strategy;

import com.onair.hearit.common.log.mask.MaskingType;
import com.onair.hearit.common.log.mask.UrlMasker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileUrlMaskingStrategy implements MaskingStrategy {

    private final UrlMasker urlMasker;

    @Override
    public MaskingType type() {
        return MaskingType.URL;
    }

    @Override
    public String mask(String value) {
        return urlMasker.maskUrl(value);
    }
}
