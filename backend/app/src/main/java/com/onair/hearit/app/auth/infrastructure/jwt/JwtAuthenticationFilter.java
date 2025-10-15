package com.onair.hearit.app.auth.infrastructure.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.app.auth.domain.RequestUser;
import com.onair.hearit.app.exception.ErrorCode;
import com.onair.hearit.core.log.logger.ConsoleLogger;
import com.onair.hearit.core.log.logger.JsonLogger;
import com.onair.hearit.core.log.property.api.ExceptionLogProperty;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Deprecated(since = "android version 1.3.0", forRemoval = true)
    private static final String DEVICE_UUID_HEADER = "X-Device-UUID";

    private static final String DEVICE_UUID_HEADER_V2 = "Device-Uuid";

    private final JsonLogger jsonLogger;
    private final ConsoleLogger consoleLogger;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<String> whitelist;
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        MDC.put("loggedByAop", "true");
        try {
            String token = extractTokenFromHeader(request.getHeader("Authorization"));

            if ((token == null || token.isBlank()) && isWhitelisted(request)) {
                authenticateAsGuest(request);
                chain.doFilter(request, response);
                return;
            }

            TokenStatus tokenStatus = jwtTokenProvider.getTokenStatus(token);
            switch (tokenStatus) {
                case NOT_EXIST -> handleAuthenticatedRequiredError(response, request);
                case EXPIRED -> handleTokenExpiredError(response, request);
                case INVALID -> handleInvalidTokenError(response, request);
                case VALID -> {
                    authenticateAsMember(token);
                    chain.doFilter(request, response);
                }
            }
        } finally {
            clearMdc();
        }
    }

    private String extractTokenFromHeader(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring("Bearer ".length());
    }

    private boolean isWhitelisted(HttpServletRequest request) {
        String path = request.getRequestURI();
        return whitelist.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private void authenticateAsGuest(HttpServletRequest request) {
        String deviceUuid = getDeviceUuid(request);
        RequestUser guestUser = RequestUser.guest(deviceUuid);

        setMdcForUser(guestUser);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(guestUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private static String getDeviceUuid(HttpServletRequest request) {
        String deviceUuid = request.getHeader(DEVICE_UUID_HEADER_V2);
        if (deviceUuid == null) {
            return request.getHeader(DEVICE_UUID_HEADER);
        }
        return deviceUuid;
    }

    private void authenticateAsMember(String token) {
        Long memberId = jwtTokenProvider.getMemberId(token);
        RequestUser memberUser = RequestUser.member(memberId);

        setMdcForUser(memberUser);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(memberUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void setMdcForUser(RequestUser user) {
        MDC.put("userType", user.getUserType());
        MDC.put("memberId", String.valueOf(user.getMemberId()));
        MDC.put("guestId", String.valueOf(user.getGuestId()));
    }

    private void clearMdc() {
        MDC.remove("userType");
        MDC.remove("memberId");
        MDC.remove("guestId");
    }

    private void handleAuthenticatedRequiredError(HttpServletResponse response, HttpServletRequest request)
            throws IOException {
        ProblemDetail problemDetail = buildProblemDetail(ErrorCode.AUTHENTICATION_REQUIRED, "인증이 필요한 요청입니다.", request);
        writeProblemDetailResponse(response, problemDetail);
        logWarn(request, problemDetail);
    }

    private void handleTokenExpiredError(HttpServletResponse response, HttpServletRequest request) throws IOException {
        ProblemDetail problemDetail = buildProblemDetail(ErrorCode.ACCESS_TOKEN_EXPIRED, "만료된 토큰입니다.", request);
        writeProblemDetailResponse(response, problemDetail);
        logWarn(request, problemDetail);
    }

    private void handleInvalidTokenError(HttpServletResponse response, HttpServletRequest request) throws IOException {
        ProblemDetail problemDetail = buildProblemDetail(ErrorCode.INVALID_ACCESS_TOKEN, "유효하지 않은 토큰입니다.", request);
        writeProblemDetailResponse(response, problemDetail);
        logWarn(request, problemDetail);
    }

    private void logWarn(HttpServletRequest request, ProblemDetail problemDetail) {
        ExceptionLogProperty exceptionLogProperty = ExceptionLogProperty.warnFromProblemDetail(request.getRequestURI(),
                request.getMethod(), problemDetail);
        jsonLogger.warn(exceptionLogProperty);
        consoleLogger.warn(exceptionLogProperty);
    }

    private ProblemDetail buildProblemDetail(ErrorCode errorCode, String detail, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(errorCode.getHttpStatus(), detail);
        problemDetail.setTitle(errorCode.getHttpStatus().getReasonPhrase());
        problemDetail.setType(URI.create(request.getRequestURI()));
        String code = errorCode.name();
        boolean reissuable = (errorCode == ErrorCode.ACCESS_TOKEN_EXPIRED);
        problemDetail.setProperty("code", code);
        problemDetail.setProperty("reissuable", reissuable);
        Map<String, Object> properties = Map.of(
                "code", code,
                "reissuable", reissuable
        );
        problemDetail.setProperty("properties", properties);
        return problemDetail;
    }

    // Filter의 경우 GlobalExceptionHandler에서 잡히지 않기 때문에 스스로 처리해야됨
    private void writeProblemDetailResponse(HttpServletResponse response, ProblemDetail problemDetail)
            throws IOException {
        response.setStatus(problemDetail.getStatus());
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        String body = objectMapper.writeValueAsString(problemDetail);
        response.getWriter().write(body);
    }
}
