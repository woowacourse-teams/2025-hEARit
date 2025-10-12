package com.onair.hearit.core.log.exception;

import com.onair.hearit.core.log.dto.logproperty.ExceptionLogProperty;
import com.onair.hearit.core.log.logger.ConsoleLogger;
import com.onair.hearit.core.log.logger.JsonLogger;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class FilterExceptionLogger extends OncePerRequestFilter {

    private final ConsoleLogger consoleLogger;
    private final JsonLogger jsonLogger;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (RuntimeException ex) {
            ExceptionLogProperty exceptionLogProperty =
                    ExceptionLogProperty.errorFromThrowable(
                            request.getRequestURI(),
                            request.getMethod(),
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            ex);

            jsonLogger.error(exceptionLogProperty);
            consoleLogger.error(exceptionLogProperty);
            throw ex;
        }
    }
}
