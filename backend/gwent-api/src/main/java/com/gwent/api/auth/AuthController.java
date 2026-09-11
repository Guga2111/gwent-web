package com.gwent.api.auth;

import com.gwent.api.security.CookieUtil;
import com.gwent.api.security.JwtTokenUtil;
import com.gwent.api.security.RefreshResult;
import com.gwent.api.security.RefreshTokenService;
import com.gwent.api.security.SecurityConstants;
import com.gwent.api.user.User;
import com.gwent.api.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final String jwtSecret;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public AuthController(UserService userService, RefreshTokenService refreshTokenService,
                          @Value("${jwt.secret}") String jwtSecret,
                          @Value("${jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
                          @Value("${jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
        this.jwtSecret = jwtSecret;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        User user = userService.registerUser(request.email(), request.username(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegisterResponse(user.getId(), user.getEmail(), user.getUsername()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        String oldToken = CookieUtil.readRefreshTokenCookie(request);
        if (oldToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        RefreshResult result = refreshTokenService.rotate(oldToken);

        User user = userService.getUser(result.email());
        String accessToken = JwtTokenUtil.createAccessToken(
                result.email(), result.userId(), user.getUsername(), jwtSecret, accessTokenExpirationMs);

        response.addHeader(SecurityConstants.AUTHORIZATION, SecurityConstants.BEARER + accessToken);
        CookieUtil.addRefreshTokenCookie(response, result.newTokenValue(), refreshTokenExpirationMs);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = CookieUtil.readRefreshTokenCookie(request);
        if (token != null) {
            refreshTokenService.invalidate(token);
        }
        CookieUtil.clearRefreshTokenCookie(response);
        return ResponseEntity.noContent().build();
    }
}
