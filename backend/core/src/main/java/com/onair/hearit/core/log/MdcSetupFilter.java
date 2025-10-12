package com.onair.hearit.core.log;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 필터 체인에서 가장 먼저 실행되고 가장 마지막에 finally가 호출되는 Servlet Filter.
 * <p>
 * 이는 요청 처리 중 유지되어야 하는 MDC 값이 의도치 않게 삭제되지 않도록 하기 위함이다.
 * </p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcSetupFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        MDC.put("ip", httpServletRequest.getRemoteAddr());
        MDC.put("timestamp", LocalDateTime.now(ZoneId.of("Asia/Seoul")).toString());
        MDC.put("deviceModel", httpServletRequest.getHeader("Device-Model"));
        MDC.put("appVersion", httpServletRequest.getHeader("App-Version"));
        MDC.put("guestId", httpServletRequest.getHeader("X-Device-UUID"));
        MDC.put("userType", httpServletRequest.getHeader("unspecified"));

        /* latencyTime을 위한 value */
        MDC.put("startTime", String.valueOf(System.currentTimeMillis()));
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
