package com.rsp.battle.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void save(String refreshToken, Long userId, long expireMillis) {
        redisTemplate.opsForValue()
                .set(
                        getKey(refreshToken),
                        userId.toString(),
                        expireMillis,
                        TimeUnit.MILLISECONDS
                );
    }

    public String getUserId(String refreshToken) {
        return (String) redisTemplate.opsForValue().get(getKey(refreshToken));
    }

    public void delete(String refreshToken) {
        redisTemplate.delete(getKey(refreshToken));
    }

    private String getKey(String token) {
        return "refresh:" + token;
    }
}