package com.rsp.battle.auth.domain;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccessTokenTest {

    @Test
    void constructorRejectsBlankTokenValue() {
        assertThrows(IllegalArgumentException.class, () -> new AccessToken(" ", 1000L));
    }

    @Test
    void constructorRejectsNonPositiveExpiry() {
        assertThrows(IllegalArgumentException.class, () -> new AccessToken("token", 0L));
    }

    @Test
    void maxAgeSecondsConvertsMillisToSeconds() {
        AccessToken accessToken = new AccessToken("token", 30_000L);

        assertEquals(30L, accessToken.maxAgeSeconds());
    }
}
