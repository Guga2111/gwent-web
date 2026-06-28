package com.gwent.api.security.filters;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gwent.api.security.SecurityConstants;
import com.gwent.api.user.User;
import com.gwent.api.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final String jwtSecret;
    private final long jwtExpirationMs;

    public AuthenticationFilter(AuthenticationManager authenticationManager, UserService userService,
                                String jwtSecret, long jwtExpirationMs) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
        setFilterProcessesUrl("/authenticate");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> credentials = new ObjectMapper().readValue(request.getInputStream(), Map.class);
            String email = credentials.get("email");
            String password = credentials.get("password");
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password, List.of())
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to read authentication request", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) {
        String email = authResult.getName();
        User user = userService.getUser(email);

        String token = JWT.create()
                .withSubject(email)
                .withClaim("userId", user.getId().toString())
                .withClaim("username", user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .sign(Algorithm.HMAC512(jwtSecret));

        response.addHeader(SecurityConstants.AUTHORIZATION, SecurityConstants.BEARER + token);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + failed.getMessage() + "\"}");
    }
}