package com.rsp.battle.auth.infrastructure;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthorizationCodeRepository {

    private static final String PREFIX = "oauth:code:";
    private static final Duration TTL = Duration.ofMinutes(3);

    private final RedisTemplate<String, Object> redisTemplate;

    public AuthorizationCodeRepository(
            @Qualifier("oauthRedisTemplate")
            RedisTemplate<String, Object> redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    public void save(String code, long userId) {
        redisTemplate.opsForValue().set(PREFIX + code, String.valueOf(userId), TTL);
    }

    public String consumeUserId(String code) {
        String key = PREFIX + code;
        Object userId = redisTemplate.opsForValue().get(key);
        redisTemplate.delete(key);
        return userId == null ? null : userId.toString();
    }
}
