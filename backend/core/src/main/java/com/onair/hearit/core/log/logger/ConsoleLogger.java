package com.onair.hearit.core.log.logger;

import com.onair.hearit.core.log.dto.RequestLogProperty;
import com.onair.hearit.core.log.dto.ResponseLogProperty;
import com.onair.hearit.core.log.formatter.ConsoleLogFormatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class ConsoleLogger {

    private static final Logger consoleLogger = LogManager.getLogger("consoleLogger");

    public void debug(Object object) {
        consoleLogger.debug(object);
    }

    public void info(Object object) {
        if (object instanceof RequestLogProperty requestLogProperty) {
            consoleLogger.info(ConsoleLogFormatter.formatRequestLogProperty(requestLogProperty));
            return;
        }
        if (object instanceof ResponseLogProperty responseLogProperty) {
            consoleLogger.info(ConsoleLogFormatter.formatResponseLogProperty(responseLogProperty));
            return;
        }
        consoleLogger.info(object);
    }

    public void warn(Object object) {
        consoleLogger.warn(object);
    }

    public void warn(String format, Object... args) {
        consoleLogger.warn(format, args);
    }

    public void error(Object object, Throwable throwable) {
        consoleLogger.error(object, throwable);
    }

    public void error(String format, Object... args) {
        consoleLogger.error(format, args);
    }

    public void error(Object object) {
        consoleLogger.error(object);
    }
}
