package com.rsp.battle.auth.refresh;

import com.rsp.battle.auth.domain.RefreshToken;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RefreshTokenTest {

    @Test
    void constructorRejectsBlankToken() {
        assertThrows(IllegalArgumentException.class, () -> new RefreshToken(" "));
    }

    @Test
    void fromNullableReturnsEmptyWhenTokenMissing() {
        assertTrue(RefreshToken.fromNullable(null).isEmpty());
        assertTrue(RefreshToken.fromNullable(" ").isEmpty());
    }

    @Test
    void redisKeyBuildsPrefixedKey() {
        RefreshToken refreshToken = new RefreshToken("token-123");

        assertEquals("refresh:token-123", refreshToken.redisKey());
    }
}
