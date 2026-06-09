package com.company.backendinc.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    private final CorsConfigurationSource corsConfigurationSource;
    private final CustomAuthenticationProvider customAuthenticationProvider;
    private final CustomTokenAuthenticationFilter customTokenAuthenticationFilter;

    public SecurityConfiguration(
            CorsConfigurationSource corsConfigurationSource,
            CustomAuthenticationProvider customAuthenticationProvider,
            CustomTokenAuthenticationFilter customTokenAuthenticationFilter) {
        this.corsConfigurationSource = corsConfigurationSource;
        this.customAuthenticationProvider = customAuthenticationProvider;
        this.customTokenAuthenticationFilter = customTokenAuthenticationFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(customAuthenticationProvider);
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(customTokenAuthenticationFilter, BasicAuthenticationFilter.class)
                .authorizeHttpRequests(authz -> authz
                        // Allow unauthenticated access to all auth endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        // Allow unauthenticated access to mailbox trace endpoint (startup flow)
                        .requestMatchers("POST", "/api/mailboxes/graph/user/trace").permitAll()
                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}
