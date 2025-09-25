package com.onair.hearit.auth.infrastructure.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.auth.domain.RequestUser;
import com.onair.hearit.exception.ErrorCode;
import com.onair.hearit.log.exception.FilterExceptionLogger;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String DEVICE_UUID_HEADER = "X-Device-UUID";

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<String> whitelist;
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final FilterExceptionLogger filterExceptionLogger;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String token = extractTokenFromHeader(request.getHeader("Authorization"));

        // 화이트리스트면 그냥 통과
        if ((token == null || token.isBlank()) && isWhitelisted(request)) {
            authenticateAsGuest(request);
            chain.doFilter(request, response);
            return;
        }

        // 토큰이 헤더에 존재하거나 인증이 필요한 엔드포인트 처리
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
        String deviceUuid = request.getHeader(DEVICE_UUID_HEADER);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(RequestUser.guest(deviceUuid), null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);
        log.info("비회원으로 접속 완료, deviceUuid={}", deviceUuid);
    }

    private void authenticateAsMember(String token) {
        Long memberId = jwtTokenProvider.getMemberId(token);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(RequestUser.member(memberId), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
        log.info("회원으로 인증 완료, memberId={}", memberId);
    }

    private void handleAuthenticatedRequiredError(HttpServletResponse response, HttpServletRequest request)
            throws IOException {
        ProblemDetail problemDetail = buildProblemDetail(ErrorCode.AUTHENTICATION_REQUIRED, "인증이 필요한 요청입니다.", request);
        writeProblemDetailResponse(response, problemDetail);
        filterExceptionLogger.warn(problemDetail);
    }

    private void handleTokenExpiredError(HttpServletResponse response, HttpServletRequest request) throws IOException {
        ProblemDetail problemDetail = buildProblemDetail(ErrorCode.ACCESS_TOKEN_EXPIRED, "만료된 토큰입니다.", request);
        writeProblemDetailResponse(response, problemDetail);
        filterExceptionLogger.warn(problemDetail);
    }

    private void handleInvalidTokenError(HttpServletResponse response, HttpServletRequest request) throws IOException {
        ProblemDetail problemDetail = buildProblemDetail(ErrorCode.INVALID_ACCESS_TOKEN, "유효하지 않은 토큰입니다.", request);
        writeProblemDetailResponse(response, problemDetail);
        filterExceptionLogger.warn(problemDetail);
    }

    private ProblemDetail buildProblemDetail(ErrorCode errorCode, String detail, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(errorCode.getHttpStatus(), detail);
        problemDetail.setTitle(errorCode.getTitle());
        problemDetail.setType(URI.create(request.getRequestURI()));
        problemDetail.setProperty("code", errorCode.name());
        problemDetail.setProperty("reissuable", errorCode == ErrorCode.ACCESS_TOKEN_EXPIRED);
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
