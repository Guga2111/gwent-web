package com.gwent.api.security.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(
        boolean enabled,
        TierProperties auth,
        TierProperties publicTier,
        TierProperties authenticated
) {

    public record TierProperties(int capacity, int refillRate, long refillIntervalMs) {
    }
}
