package org.astu.attendancetracker.core.application.config;

import lombok.RequiredArgsConstructor;
import org.astu.attendancetracker.core.application.auth.JwtAuthenticationFilter;
import org.astu.attendancetracker.core.domain.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    private static final String[] VAADIN_WHITELIST = {
            "/VAADIN/**",
            "/frontend/**",
            "/frontend-es5/**", "/frontend-es6/**",
            "/favicon.ico",
            "/icons/**",
            "/images/**",
            "/manifest.webmanifest",
            "/sw.js",
            "/sw-runtime-resources-precache.js"
    };

    private static final String[] WHITE_LIST_URL = {
            "/",
            "/home",
            "/groups",

            "/api/v1/auth/**",
            "/api/v1/group/**",
            "/api/v1/profile/**",
            "/api/v1/lesson/**",

            "/v2/api-docs",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-ui.html"};

    private static final String[] STUDENT_LIST_URL = { };
    private static final String[] TEACHER_LIST_URL = { };
    private static final String[] ADMIN_LIST_URL = { };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> req
                        .requestMatchers(VAADIN_WHITELIST)
                        .permitAll()

                        .requestMatchers(WHITE_LIST_URL)
                        .permitAll()

                        .requestMatchers(STUDENT_LIST_URL)
                        .hasAuthority(Role.STUDENT.name())

                        .requestMatchers(TEACHER_LIST_URL)
                        .hasAuthority(Role.TEACHER.name())

                        .requestMatchers(ADMIN_LIST_URL)
                        .hasAuthority(Role.ADMIN.name())

                        .anyRequest()
                        .authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
