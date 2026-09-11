package com.gwent.api.security.ratelimit;

public record RateLimitResult(boolean allowed, long remainingTokens, long retryAfterMs) {
}
