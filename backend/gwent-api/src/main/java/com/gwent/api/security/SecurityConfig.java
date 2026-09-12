package com.gwent.api.security;

import com.gwent.api.security.filters.AuthenticationFilter;
import com.gwent.api.security.filters.ExceptionHandlerFilter;
import com.gwent.api.security.filters.JWTAuthorizationFilter;
import com.gwent.api.security.manager.CustomAuthManager;
import com.gwent.api.security.ratelimit.RateLimitFilter;
import com.gwent.api.user.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomAuthManager customAuthManager,
                                                   UserService userService,
                                                   RefreshTokenService refreshTokenService,
                                                   RateLimitFilter rateLimitFilter) throws Exception {
        AuthenticationFilter authenticationFilter =
                new AuthenticationFilter(customAuthManager, userService, refreshTokenService,
                        jwtSecret, accessTokenExpirationMs, refreshTokenExpirationMs);

        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, SecurityConstants.REGISTER_PATH).permitAll()
                        .requestMatchers(HttpMethod.POST, "/authenticate").permitAll()
                        .requestMatchers(HttpMethod.POST, SecurityConstants.REFRESH_PATH).permitAll()
                        .requestMatchers(HttpMethod.POST, SecurityConstants.LOGOUT_PATH).permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/", "/index.html").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new ExceptionHandlerFilter(), AuthenticationFilter.class)
                .addFilterBefore(rateLimitFilter, ExceptionHandlerFilter.class)
                .addFilter(authenticationFilter)
                .addFilterAfter(new JWTAuthorizationFilter(jwtSecret), AuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
