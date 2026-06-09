package dk.ek.gameapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenRevocationService {

    private final StringRedisTemplate redisTemplate;
    private static final String REVOKED_TOKEN_PREFIX = "revoked:";
    private static final long TOKEN_EXPIRY_BUFFER = 3600; // 1 hour buffer

    public void revokeToken(String tokenId, long expirySeconds) {
        String key = REVOKED_TOKEN_PREFIX + tokenId;
        redisTemplate.opsForValue().set(key, "revoked", expirySeconds + TOKEN_EXPIRY_BUFFER, TimeUnit.SECONDS);
    }

    public boolean isTokenRevoked(String tokenId) {
        String key = REVOKED_TOKEN_PREFIX + tokenId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}