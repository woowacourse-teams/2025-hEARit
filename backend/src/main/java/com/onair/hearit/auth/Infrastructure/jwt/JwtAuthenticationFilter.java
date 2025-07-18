package com.onair.hearit.auth.Infrastructure.jwt;

import com.onair.hearit.auth.dto.CurrentMember;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        String token = extractTokenFromHeader(header);
        if (!jwtTokenProvider.validateToken(token)) {
            chain.doFilter(request, response);
            return;
        }

        Long memberId = jwtTokenProvider.getMemberId(token);
        CurrentMember currentMember = new CurrentMember(memberId);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(currentMember, null, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(request, response);
    }

    private String extractTokenFromHeader(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring("Bearer ".length());
    }
}
