package com.rsp.battle.user.persistence;

import com.rsp.battle.user.domain.PresenceStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PresenceRepositoryImplTest {

    @Mock
    private RedisTemplate<String, PresenceStatus> redisTemplate;

    @Mock
    private ValueOperations<String, PresenceStatus> valueOperations;

    @InjectMocks
    private PresenceRepositoryImpl presenceRepository;

    @Test
    void setPresenceStatusOnline_setsOnlineWhenNotInBattle() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("presence:1")).thenReturn(PresenceStatus.ONLINE);

        presenceRepository.setPresenceStatusOnline(1L);

        verify(valueOperations).set("presence:1", PresenceStatus.ONLINE, 30, TimeUnit.SECONDS);
    }

    @Test
    void setPresenceStatusOnline_setsOnlineWhenKeyNotExists() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("presence:1")).thenReturn(null);

        presenceRepository.setPresenceStatusOnline(1L);

        verify(valueOperations).set("presence:1", PresenceStatus.ONLINE, 30, TimeUnit.SECONDS);
    }

    @Test
    void setPresenceStatusOnline_skipsWhenInBattle() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("presence:1")).thenReturn(PresenceStatus.IN_BATTLE);

        presenceRepository.setPresenceStatusOnline(1L);

        verify(valueOperations, never()).set(anyString(), any(PresenceStatus.class), anyLong(), any(TimeUnit.class));
    }

    @Test
    void getPresenceStatus_returnsStatusWhenKeyExists() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("presence:1")).thenReturn(PresenceStatus.ONLINE);

        PresenceStatus result = presenceRepository.getPresenceStatus(1L);

        assertEquals(PresenceStatus.ONLINE, result);
    }

    @Test
    void getPresenceStatus_returnsOfflineWhenKeyNotExists() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("presence:1")).thenReturn(null);

        PresenceStatus result = presenceRepository.getPresenceStatus(1L);

        assertEquals(PresenceStatus.OFFLINE, result);
    }

    @Test
    void getPresenceStatuses_returnsBulkStatuses() {
        List<Long> userIds = List.of(1L, 2L, 3L);
        List<String> keys = List.of("presence:1", "presence:2", "presence:3");
        List<PresenceStatus> values = Arrays.asList(PresenceStatus.ONLINE, null, PresenceStatus.IN_BATTLE);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.multiGet(keys)).thenReturn(values);

        Map<Long, PresenceStatus> result = presenceRepository.getPresenceStatuses(userIds);

        assertEquals(PresenceStatus.ONLINE, result.get(1L));
        assertEquals(PresenceStatus.OFFLINE, result.get(2L));
        assertEquals(PresenceStatus.IN_BATTLE, result.get(3L));
    }
}
