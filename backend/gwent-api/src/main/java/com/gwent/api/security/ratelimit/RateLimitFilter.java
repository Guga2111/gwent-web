package com.gwent.api.security.ratelimit;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.gwent.api.security.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.lettuce.core.RedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private static final Set<String> AUTH_PATHS = Set.of(
            "/authenticate",
            SecurityConstants.REGISTER_PATH,
            SecurityConstants.REFRESH_PATH,
            SecurityConstants.LOGOUT_PATH
    );

    private final RateLimitService rateLimitService;
    private final boolean enabled;

    public RateLimitFilter(RateLimitService rateLimitService, boolean enabled) {
        this.rateLimitService = rateLimitService;
        this.enabled = enabled;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!enabled || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        if (path.startsWith("/ws")) {
            filterChain.doFilter(request, response);
            return;
        }

        RateLimitTier tier = resolveTier(request);
        String identifier = resolveIdentifier(request);

        RateLimitResult result;
        try {
            result = rateLimitService.tryConsume(tier, identifier);
        } catch (RedisException e) {
            log.warn("Redis unavailable in rate limit filter, failing open: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        long capacity = rateLimitService.getCapacity(tier);
        response.setHeader("X-RateLimit-Limit", String.valueOf(capacity));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remainingTokens()));

        if (result.retryAfterMs() > 0) {
            long retryAfterSeconds = (long) Math.ceil(result.retryAfterMs() / 1000.0);
            response.setHeader("X-RateLimit-Reset", String.valueOf(retryAfterSeconds));
        }

        if (!result.allowed()) {
            long retryAfterSeconds = (long) Math.ceil(result.retryAfterMs() / 1000.0);
            response.setStatus(429);
            response.setIntHeader("Retry-After", (int) retryAfterSeconds);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests. Please retry after " + retryAfterSeconds + " seconds.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private RateLimitTier resolveTier(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (AUTH_PATHS.contains(path)) {
            return RateLimitTier.AUTH;
        }

        String authHeader = request.getHeader(SecurityConstants.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(SecurityConstants.BEARER)) {
            return RateLimitTier.AUTHENTICATED;
        }

        return RateLimitTier.PUBLIC;
    }

    private String resolveIdentifier(HttpServletRequest request) {
        String authHeader = request.getHeader(SecurityConstants.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(SecurityConstants.BEARER)) {
            try {
                String token = authHeader.substring(SecurityConstants.BEARER.length());
                DecodedJWT decoded = JWT.decode(token);
                String subject = decoded.getSubject();
                if (subject != null && !subject.isBlank()) {
                    return subject;
                }
            } catch (Exception e) {
                log.debug("Failed to decode JWT for rate limiting, falling back to IP: {}", e.getMessage());
            }
        }

        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
