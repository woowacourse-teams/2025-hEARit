package com.onair.hearit.auth.infrastructure.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.auth.dto.CurrentMember;
import com.onair.hearit.common.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final List<String> whitelist;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        String token = extractTokenFromHeader(header);

        // 화이트리스트면 토큰이 없어도 그냥 통과
        if ((token == null || token.isBlank()) && isWhitelisted(request)) {
            chain.doFilter(request, response);
            return;
        }

        if (!jwtTokenProvider.validateToken(token)) {
            ProblemDetail problemDetail = buildProblemDetail(ErrorCode.UNAUTHORIZED, "유효하지 않은 토큰입니다.", request);
            writeProblemDetailResponse(response, problemDetail);
            return;
        }

        Long memberId = jwtTokenProvider.getMemberId(token);
        CurrentMember currentMember = new CurrentMember(memberId);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(currentMember, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(request, response);
    }

    private boolean isWhitelisted(HttpServletRequest request) {
        String path = request.getRequestURI();
        return whitelist.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private String extractTokenFromHeader(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring("Bearer ".length());
    }

    private ProblemDetail buildProblemDetail(ErrorCode errorCode, String detail, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(errorCode.getHttpStatus(), detail);
        problemDetail.setTitle(errorCode.getTitle());
        problemDetail.setType(URI.create(request.getRequestURI()));
        return problemDetail;
    }

    // Filter의 경우 GlobalExceptionHandler에서 잡히지 않기 때문에 스스로 처리해야됨
    private void writeProblemDetailResponse(HttpServletResponse response, ProblemDetail problemDetail)
            throws IOException {
        response.setStatus(problemDetail.getStatus());
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");

        ObjectMapper objectMapper = new ObjectMapper();
        String body = objectMapper.writeValueAsString(problemDetail);
        response.getWriter().write(body);
    }
}
