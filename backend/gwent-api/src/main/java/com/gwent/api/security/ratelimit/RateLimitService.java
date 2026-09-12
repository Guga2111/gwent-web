package com.gwent.api.security.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Supplier;

@Service
public class RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);
    private static final String KEY_PREFIX = "rate_limit:";

    private final ProxyManager<byte[]> proxyManager;
    private final RateLimitProperties properties;

    public RateLimitService(LettuceConnectionFactory connectionFactory, RateLimitProperties properties) {
        this.properties = properties;

        RedisClient redisClient = (RedisClient) connectionFactory.getNativeClient();
        this.proxyManager = Bucket4jLettuce.casBasedBuilder(redisClient)
                .expirationAfterWrite(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(
                        Duration.ofMinutes(1)))
                .build()
                .asProxyManager();
    }

    public RateLimitResult tryConsume(RateLimitTier tier, String identifier) {
        byte[] key = (KEY_PREFIX + tier.name().toLowerCase() + ":" + identifier).getBytes(StandardCharsets.UTF_8);
        Supplier<BucketConfiguration> configSupplier = () -> buildConfig(tier);

        try {
            ConsumptionProbe probe = proxyManager.builder()
                    .build(key, configSupplier)
                    .tryConsumeAndReturnRemaining(1);

            return new RateLimitResult(
                    probe.isConsumed(),
                    probe.getRemainingTokens(),
                    probe.isConsumed() ? 0 : nanosToMs(probe.getNanosToWaitForRefill())
            );
        } catch (RedisException e) {
            log.warn("Redis unavailable for rate limiting, failing open: {}", e.getMessage());
            return new RateLimitResult(true, -1, 0);
        }
    }

    public long getCapacity(RateLimitTier tier) {
        return resolveConfig(tier).capacity();
    }

    private BucketConfiguration buildConfig(RateLimitTier tier) {
        RateLimitProperties.TierProperties config = resolveConfig(tier);
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(config.capacity())
                        .refillGreedy(config.refillRate(), Duration.ofMillis(config.refillIntervalMs()))
                        .build())
                .build();
    }

    private RateLimitProperties.TierProperties resolveConfig(RateLimitTier tier) {
        return switch (tier) {
            case AUTH -> properties.auth();
            case PUBLIC -> properties.publicTier();
            case AUTHENTICATED -> properties.authenticated();
        };
    }

    private static long nanosToMs(long nanos) {
        return (long) Math.ceil(nanos / 1_000_000.0);
    }
}
