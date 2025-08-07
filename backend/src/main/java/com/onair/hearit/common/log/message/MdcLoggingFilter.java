package com.onair.hearit.common.log.message;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;
import org.apache.logging.log4j.core.config.Order;
import org.jboss.logging.MDC;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        MDC.put("id", UUID.randomUUID().toString());
        MDC.put("ip", httpServletRequest.getRemoteAddr());
        MDC.put("httpMethod", httpServletRequest.getMethod());
        MDC.put("requestUri", httpServletRequest.getRequestURI());
        MDC.put("startTime", String.valueOf(System.currentTimeMillis()));

        chain.doFilter(request, response);

        MDC.clear();
    }
}
