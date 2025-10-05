package com.onair.hearit.app.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.app.auth.infrastructure.jwt.JwtAuthenticationFilter;
import com.onair.hearit.app.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.core.log.exception.FilterExceptionLogger;
import java.util.Arrays;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class ApiSecurityConfig {

    private static final String[] PUBLIC_AUTH_ENDPOINTS = {
            "/api/*/auth/login",
            "/api/*/auth/kakao-login",
            "/api/*/auth/signup",
            "/api/*/auth/token/refresh",
    };

    private static final String[] PUBLIC_GET_ENDPOINTS = {
            "/api/*/hearits/**",
            "/api/*/categories/**",
            "/api/*/keywords/**",
            "/api/*/playing-histories/**",
            "/api/*/bookmarks",
            "/api/*/recommendations/**",
    };

    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final FilterExceptionLogger filterExceptionLogger;

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .securityMatcher("/api/**")
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_ENDPOINTS).permitAll()
                        .requestMatchers(PUBLIC_AUTH_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                ).addFilterBefore(
                        new JwtAuthenticationFilter(
                                Stream.concat(
                                        Arrays.stream(PUBLIC_GET_ENDPOINTS),
                                        Arrays.stream(PUBLIC_AUTH_ENDPOINTS)).toList(),
                                objectMapper,
                                jwtTokenProvider,
                                filterExceptionLogger),
                        UsernamePasswordAuthenticationFilter.class
                ).build();
    }
}
