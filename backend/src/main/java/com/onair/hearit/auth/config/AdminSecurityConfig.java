package com.onair.hearit.auth.config;

import static org.springframework.security.config.Customizer.withDefaults;

import com.onair.hearit.auth.application.AdminUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Order(1)
@Configuration
@RequiredArgsConstructor
public class AdminSecurityConfig {

    private final AdminUserDetailsService adminUserDetailsService;

    @Bean
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(withDefaults())
                .securityMatcher("/admin/**", "/api/v1/admin/**")
                .userDetailsService(adminUserDetailsService)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/login").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .usernameParameter("loginId")
                        .passwordParameter("password")
                        .loginProcessingUrl("/admin/login")
                        .defaultSuccessUrl("/admin", true)
                )
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .build();
    }
}
