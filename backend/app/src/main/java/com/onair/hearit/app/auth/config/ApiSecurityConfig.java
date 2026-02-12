package com.onair.hearit.app.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onair.hearit.app.auth.infrastructure.jwt.JwtAuthenticationFilter;
import com.onair.hearit.app.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.core.log.MdcSetupFilter;
import com.onair.hearit.core.log.RequestLoggingFallbackFilter;
import com.onair.hearit.core.log.logger.ConsoleLogger;
import com.onair.hearit.core.log.logger.JsonLogger;
import java.util.Arrays;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
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
            "/api/*/advertisements/**",
    };

    private final JsonLogger jsonLogger;
    private final ConsoleLogger consoleLogger;
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.version}")
    private String serverVersion;

    @Bean
    public MdcSetupFilter mdcSetupFilter() {
        return new MdcSetupFilter(serverVersion);
    }

    @Bean
    public FilterRegistrationBean<MdcSetupFilter> mdcSetupFilterRegistration(MdcSetupFilter mdcSetupFilter) {
        FilterRegistrationBean<MdcSetupFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(mdcSetupFilter);
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

    @Bean
    public RequestLoggingFallbackFilter requestLoggingFilter() {
        return new RequestLoggingFallbackFilter(jsonLogger, consoleLogger);
    }

    @Bean
    public FilterRegistrationBean<RequestLoggingFallbackFilter> requestLoggingFallbackFilterRegistration(
            RequestLoggingFallbackFilter requestLoggingFallbackFilter) {
        FilterRegistrationBean<RequestLoggingFallbackFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(requestLoggingFallbackFilter);
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

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
                                jsonLogger,
                                consoleLogger,
                                Stream.concat(
                                        Arrays.stream(PUBLIC_GET_ENDPOINTS),
                                        Arrays.stream(PUBLIC_AUTH_ENDPOINTS)).toList(),
                                objectMapper,
                                jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                ).build();
    }
}
