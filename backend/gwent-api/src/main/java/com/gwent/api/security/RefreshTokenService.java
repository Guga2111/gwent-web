package com.gwent.api.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final Duration REFRESH_TTL = Duration.ofDays(7);
    private static final String TOKEN_PREFIX = "refresh_token:";
    private static final String FAMILY_PREFIX = "token_family:";
    private static final String USER_FAMILIES_PREFIX = "user_families:";

    private final StringRedisTemplate redis;

    public RefreshTokenService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public String createRefreshToken(String userId, String email) {
        String familyId = UUID.randomUUID().toString();
        String tokenValue = UUID.randomUUID().toString();

        redis.opsForHash().putAll(TOKEN_PREFIX + tokenValue, Map.of(
                "userId", userId,
                "email", email,
                "familyId", familyId
        ));
        redis.expire(TOKEN_PREFIX + tokenValue, REFRESH_TTL);

        redis.opsForValue().set(FAMILY_PREFIX + familyId, tokenValue, REFRESH_TTL);

        redis.opsForSet().add(USER_FAMILIES_PREFIX + userId, familyId);
        redis.expire(USER_FAMILIES_PREFIX + userId, REFRESH_TTL);

        return tokenValue;
    }

    public RefreshResult rotate(String oldTokenValue) {
        Map<Object, Object> tokenData = redis.opsForHash().entries(TOKEN_PREFIX + oldTokenValue);
        if (tokenData.isEmpty()) {
            throw new TokenReusedException("Refresh token not found — possible reuse after revocation");
        }

        String familyId = (String) tokenData.get("familyId");
        String userId = (String) tokenData.get("userId");
        String email = (String) tokenData.get("email");

        String currentToken = redis.opsForValue().get(FAMILY_PREFIX + familyId);
        if (!oldTokenValue.equals(currentToken)) {
            invalidateAllForUser(userId);
            throw new TokenReusedException("Refresh token reuse detected — all sessions invalidated");
        }

        String newTokenValue = UUID.randomUUID().toString();

        redis.opsForHash().putAll(TOKEN_PREFIX + newTokenValue, Map.of(
                "userId", userId,
                "email", email,
                "familyId", familyId
        ));
        redis.expire(TOKEN_PREFIX + newTokenValue, REFRESH_TTL);

        redis.opsForValue().set(FAMILY_PREFIX + familyId, newTokenValue, REFRESH_TTL);

        redis.delete(TOKEN_PREFIX + oldTokenValue);

        return new RefreshResult(newTokenValue, userId, email);
    }

    public void invalidate(String tokenValue) {
        Map<Object, Object> tokenData = redis.opsForHash().entries(TOKEN_PREFIX + tokenValue);
        if (tokenData.isEmpty()) return;

        String familyId = (String) tokenData.get("familyId");
        String userId = (String) tokenData.get("userId");

        redis.delete(TOKEN_PREFIX + tokenValue);
        redis.delete(FAMILY_PREFIX + familyId);
        redis.opsForSet().remove(USER_FAMILIES_PREFIX + userId, familyId);
    }

    public void invalidateAllForUser(String userId) {
        Set<String> familyIds = redis.opsForSet().members(USER_FAMILIES_PREFIX + userId);
        if (familyIds == null || familyIds.isEmpty()) return;

        for (String familyId : familyIds) {
            String tokenValue = redis.opsForValue().get(FAMILY_PREFIX + familyId);
            if (tokenValue != null) {
                redis.delete(TOKEN_PREFIX + tokenValue);
            }
            redis.delete(FAMILY_PREFIX + familyId);
        }
        redis.delete(USER_FAMILIES_PREFIX + userId);
    }
}
