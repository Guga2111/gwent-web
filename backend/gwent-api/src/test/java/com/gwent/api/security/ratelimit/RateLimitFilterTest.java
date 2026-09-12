package com.gwent.api.security.ratelimit;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.gwent.api.security.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private FilterChain filterChain;

    private RateLimitFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter(rateLimitService, true);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void shouldPassThrough_whenDisabled() throws ServletException, IOException {
        RateLimitFilter disabledFilter = new RateLimitFilter(rateLimitService, false);
        request.setRequestURI("/api/test");

        disabledFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(rateLimitService);
    }

    @Test
    void shouldPassThrough_whenOptionsRequest() throws ServletException, IOException {
        request.setMethod("OPTIONS");
        request.setRequestURI("/api/test");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(rateLimitService);
    }

    @Test
    void shouldPassThrough_whenWebSocketPath() throws ServletException, IOException {
        request.setRequestURI("/ws/game");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(rateLimitService);
    }

    @Test
    void shouldAllow_whenWithinLimit() throws ServletException, IOException {
        request.setRequestURI("/api/test");
        request.setRemoteAddr("192.168.1.1");

        when(rateLimitService.tryConsume(RateLimitTier.PUBLIC, "192.168.1.1"))
                .thenReturn(new RateLimitResult(true, 29, 0));
        when(rateLimitService.getCapacity(RateLimitTier.PUBLIC)).thenReturn(30L);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(response.getHeader("X-RateLimit-Limit")).isEqualTo("30");
        assertThat(response.getHeader("X-RateLimit-Remaining")).isEqualTo("29");
    }

    @Test
    void shouldReturn429_whenRateLimitExceeded() throws ServletException, IOException {
        request.setRequestURI("/api/test");
        request.setRemoteAddr("192.168.1.1");

        when(rateLimitService.tryConsume(RateLimitTier.PUBLIC, "192.168.1.1"))
                .thenReturn(new RateLimitResult(false, 0, 5000));
        when(rateLimitService.getCapacity(RateLimitTier.PUBLIC)).thenReturn(30L);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader("Retry-After")).isEqualTo("5");
        assertThat(response.getContentAsString()).contains("Too many requests");
    }

    @Test
    void shouldUseAuthTier_forAuthenticationPath() throws ServletException, IOException {
        request.setRequestURI("/authenticate");
        request.setRemoteAddr("10.0.0.1");

        when(rateLimitService.tryConsume(RateLimitTier.AUTH, "10.0.0.1"))
                .thenReturn(new RateLimitResult(true, 9, 0));
        when(rateLimitService.getCapacity(RateLimitTier.AUTH)).thenReturn(10L);

        filter.doFilterInternal(request, response, filterChain);

        verify(rateLimitService).tryConsume(RateLimitTier.AUTH, "10.0.0.1");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldUseAuthTier_forRegisterPath() throws ServletException, IOException {
        request.setRequestURI(SecurityConstants.REGISTER_PATH);
        request.setRemoteAddr("10.0.0.1");

        when(rateLimitService.tryConsume(RateLimitTier.AUTH, "10.0.0.1"))
                .thenReturn(new RateLimitResult(true, 9, 0));
        when(rateLimitService.getCapacity(RateLimitTier.AUTH)).thenReturn(10L);

        filter.doFilterInternal(request, response, filterChain);

        verify(rateLimitService).tryConsume(RateLimitTier.AUTH, "10.0.0.1");
    }

    @Test
    void shouldUseAuthenticatedTier_whenBearerTokenPresent() throws ServletException, IOException {
        String token = JWT.create().withSubject("user-123").sign(Algorithm.HMAC256("secret"));
        request.setRequestURI("/api/test");
        request.addHeader(SecurityConstants.AUTHORIZATION, SecurityConstants.BEARER + token);

        when(rateLimitService.tryConsume(RateLimitTier.AUTHENTICATED, "user-123"))
                .thenReturn(new RateLimitResult(true, 59, 0));
        when(rateLimitService.getCapacity(RateLimitTier.AUTHENTICATED)).thenReturn(60L);

        filter.doFilterInternal(request, response, filterChain);

        verify(rateLimitService).tryConsume(RateLimitTier.AUTHENTICATED, "user-123");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldFallBackToIp_whenTokenDecodeFails() throws ServletException, IOException {
        request.setRequestURI("/api/test");
        request.addHeader(SecurityConstants.AUTHORIZATION, SecurityConstants.BEARER + "invalid-token");
        request.setRemoteAddr("10.0.0.5");

        when(rateLimitService.tryConsume(RateLimitTier.AUTHENTICATED, "10.0.0.5"))
                .thenReturn(new RateLimitResult(true, 59, 0));
        when(rateLimitService.getCapacity(RateLimitTier.AUTHENTICATED)).thenReturn(60L);

        filter.doFilterInternal(request, response, filterChain);

        verify(rateLimitService).tryConsume(RateLimitTier.AUTHENTICATED, "10.0.0.5");
    }

    @Test
    void shouldUseXForwardedFor_whenPresent() throws ServletException, IOException {
        request.setRequestURI("/api/test");
        request.addHeader("X-Forwarded-For", "203.0.113.50, 70.41.3.18");
        request.setRemoteAddr("127.0.0.1");

        when(rateLimitService.tryConsume(RateLimitTier.PUBLIC, "203.0.113.50"))
                .thenReturn(new RateLimitResult(true, 29, 0));
        when(rateLimitService.getCapacity(RateLimitTier.PUBLIC)).thenReturn(30L);

        filter.doFilterInternal(request, response, filterChain);

        verify(rateLimitService).tryConsume(RateLimitTier.PUBLIC, "203.0.113.50");
    }

    @Test
    void shouldFailOpen_whenRedisUnavailable() throws ServletException, IOException {
        request.setRequestURI("/api/test");
        request.setRemoteAddr("10.0.0.1");

        when(rateLimitService.tryConsume(any(), any()))
                .thenThrow(new io.lettuce.core.RedisException("Connection refused"));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldSetRetryAfterHeader_whenRateLimited() throws ServletException, IOException {
        request.setRequestURI("/api/test");
        request.setRemoteAddr("10.0.0.1");

        when(rateLimitService.tryConsume(RateLimitTier.PUBLIC, "10.0.0.1"))
                .thenReturn(new RateLimitResult(false, 0, 2500));
        when(rateLimitService.getCapacity(RateLimitTier.PUBLIC)).thenReturn(30L);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getHeader("Retry-After")).isEqualTo("3");
        assertThat(response.getHeader("X-RateLimit-Reset")).isEqualTo("3");
    }

    @Test
    void shouldNotSetResetHeader_whenAllowed() throws ServletException, IOException {
        request.setRequestURI("/api/test");
        request.setRemoteAddr("10.0.0.1");

        when(rateLimitService.tryConsume(RateLimitTier.PUBLIC, "10.0.0.1"))
                .thenReturn(new RateLimitResult(true, 29, 0));
        when(rateLimitService.getCapacity(RateLimitTier.PUBLIC)).thenReturn(30L);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getHeader("X-RateLimit-Reset")).isNull();
        assertThat(response.getHeader("Retry-After")).isNull();
    }
}
