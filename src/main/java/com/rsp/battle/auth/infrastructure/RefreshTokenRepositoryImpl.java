package com.rsp.battle.auth.infrastructure;

import com.rsp.battle.auth.domain.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void save(RefreshToken token, Long userId, long expireMillis) {
        redisTemplate.opsForValue().set(
                token.redisKey(),
                userId.toString(),
                expireMillis,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public Optional<String> findUserId(RefreshToken token) {
        Object value = redisTemplate.opsForValue().get(token.redisKey());
        if (value == null) return Optional.empty();
        return Optional.of(value.toString());
    }

    @Override
    public void delete(RefreshToken token) {
        redisTemplate.delete(token.redisKey());
    }
}
