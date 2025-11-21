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
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;

/**
 * 필터 체인에서 가장 먼저 실행되고 가장 마지막에 finally가 호출되는 Servlet Filter.
 * <p>
 * 이는 요청 처리 중 유지되어야 하는 MDC 값이 의도치 않게 삭제되지 않도록 하기 위함이다.
 * </p>
 */
@RequiredArgsConstructor
public class MdcSetupFilter implements Filter {

    private final String appVersionServer;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        MDC.put("ip", httpServletRequest.getRemoteAddr());
        MDC.put("timestamp", LocalDateTime.now(ZoneId.of("Asia/Seoul")).toString());
        MDC.put("androidAppVersion", httpServletRequest.getHeader("App-Version"));
        MDC.put("serverVersion", appVersionServer);
        MDC.put("guestId", httpServletRequest.getHeader("Device-Uuid"));

        /* JwtAuthenticationFilter 에서 설정해주지만, filter 에러 발생 시 빈 값 방지를 위한 초기화 설정입니다. */
        MDC.put("userType", "unspecified");

        /* latencyTime을 위한 value */
        MDC.put("startTime", String.valueOf(System.currentTimeMillis()));
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
