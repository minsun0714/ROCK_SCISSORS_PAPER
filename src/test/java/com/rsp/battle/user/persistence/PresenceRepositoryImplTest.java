package com.rsp.battle.user.persistence;

import com.rsp.battle.user.domain.PresenceStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

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
}
