package com.onair.hearit.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.auth.infrastructure.jwt.JwtAuthenticationFilter;
import com.onair.hearit.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.log.exception.FilterExceptionLogger;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Order(2)
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class ApiSecurityConfig {

    private static final String[] PUBLIC_AUTH_ENDPOINTS = {
            "/api/v1/auth/login",
            "/api/v1/auth/kakao-login",
            "/api/v1/auth/signup",
            "/api/v1/auth/token/refresh",
    };

    private static final String[] PUBLIC_GET_ENDPOINTS = {
            "/api/v1/hearits/**",
            "/api/v1/categories/**",
            "/api/v1/keywords/**"
    };

    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final FilterExceptionLogger filterExceptionLogger;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
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
