package com.rsp.battle.user.persistence;

import com.rsp.battle.user.domain.PresenceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    @Override
    public Map<Long, PresenceStatus> getPresenceStatuses(List<Long> userIds) {
        List<String> keys = userIds.stream()
                .map(id -> "presence:" + id)
                .toList();

        List<PresenceStatus> values = redisTemplate.opsForValue().multiGet(keys);

        Map<Long, PresenceStatus> result = new LinkedHashMap<>();

        if (values == null) return result;

        for (int i = 0; i < userIds.size(); i++) {
            PresenceStatus status = values.get(i) != null
                    ? values.get(i)
                    : PresenceStatus.OFFLINE;
            result.put(userIds.get(i), status);
        }
        return result;
    }
}
