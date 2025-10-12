package com.onair.hearit.core.log.logger;

import com.onair.hearit.core.log.mask.MaskingSupport;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JsonLogger {

    private final MaskingSupport maskingSupport;
    private static final Logger jsonLogger = LogManager.getLogger("jsonLogger");

    public void debug(Object object) {
        jsonLogger.debug(maskingSupport.mask(object));
    }

    public void info(Object object) {
        jsonLogger.info(maskingSupport.mask(object));
    }

    public void warn(Object object) {
        jsonLogger.warn(maskingSupport.mask(object));
    }

    public void error(Object object, Throwable throwable) {
        jsonLogger.error(maskingSupport.mask(object), throwable);
    }

    public void error(Object object) {
        jsonLogger.error(maskingSupport.mask(object));
    }
}
