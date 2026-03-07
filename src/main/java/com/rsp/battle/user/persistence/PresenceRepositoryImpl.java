package com.rsp.battle.user.persistence;

import com.rsp.battle.user.domain.PresenceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class PresenceRepositoryImpl implements PresenceRepository{

    private final RedisTemplate<String, PresenceStatus> redisTemplate;

    private static final long TTL = 30;

    @Override
    public void setPresenceStatusOnline(Long userId) {

        String key = "presence:" + userId;

        PresenceStatus presenceStatus = redisTemplate.opsForValue().get(key);

        if (presenceStatus == PresenceStatus.IN_BATTLE) {
            return;
        }

        redisTemplate.opsForValue().set(
                key,
                PresenceStatus.ONLINE,
                TTL,
                TimeUnit.SECONDS
        );
    }

    @Override
    public PresenceStatus getPresenceStatus(Long userId) {

        String key = "presence:" + userId;

        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) return PresenceStatus.OFFLINE;

        return PresenceStatus.valueOf(value.toString());
    }
}
