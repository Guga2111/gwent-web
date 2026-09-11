package com.gwent.api.security.ratelimit;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig {

    @Bean
    public RateLimitFilter rateLimitFilter(RateLimitService rateLimitService, RateLimitProperties properties) {
        return new RateLimitFilter(rateLimitService, properties.enabled());
    }
}
