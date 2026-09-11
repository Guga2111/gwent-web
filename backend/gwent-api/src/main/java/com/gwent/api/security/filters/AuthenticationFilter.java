package com.gwent.api.security.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gwent.api.security.CookieUtil;
import com.gwent.api.security.JwtTokenUtil;
import com.gwent.api.security.RefreshTokenService;
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
import java.util.List;
import java.util.Map;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final String jwtSecret;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public AuthenticationFilter(AuthenticationManager authenticationManager, UserService userService,
                                RefreshTokenService refreshTokenService, String jwtSecret,
                                long accessTokenExpirationMs, long refreshTokenExpirationMs) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
        this.jwtSecret = jwtSecret;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
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
        String userId = user.getId().toString();

        String accessToken = JwtTokenUtil.createAccessToken(
                email, userId, user.getUsername(), jwtSecret, accessTokenExpirationMs);

        String refreshToken = refreshTokenService.createRefreshToken(userId, email);

        response.addHeader(SecurityConstants.AUTHORIZATION, SecurityConstants.BEARER + accessToken);
        CookieUtil.addRefreshTokenCookie(response, refreshToken, refreshTokenExpirationMs);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + failed.getMessage() + "\"}");
    }
}
