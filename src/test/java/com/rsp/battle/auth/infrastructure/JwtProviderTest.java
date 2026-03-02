package com.rsp.battle.auth.infrastructure;

import com.rsp.battle.auth.domain.CustomUserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    private JwtProvider createProvider() {
        byte[] keyBytes = new byte[64];
        Arrays.fill(keyBytes, (byte) 1);
        String secret = Base64.getEncoder().encodeToString(keyBytes);
        return new JwtProvider(secret);
    }

    @Test
    void createAndValidateToken() {
        JwtProvider provider = createProvider();
        String token = provider.createJwtToken(42L, 60_000L);

        assertNotNull(token);
        assertTrue(provider.validateToken(token));
    }

    @Test
    void getAuthenticationParsesSubjectAndRole() {
        JwtProvider provider = createProvider();
        String token = provider.createJwtToken(7L, 60_000L);

        Authentication authentication = provider.getAuthentication(token);
        assertNotNull(authentication);
        assertInstanceOf(CustomUserPrincipal.class, authentication.getPrincipal());

        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        assertEquals("7", principal.getUsername());

        Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();
        assertEquals(1, authorities.size());
        assertEquals("ROLE_USER", authorities.iterator().next().getAuthority());
    }

    @Test
    void validateTokenReturnsFalseForInvalidToken() {
        JwtProvider provider = createProvider();

        assertFalse(provider.validateToken("not-a-jwt"));
    }

    @Test
    void validateTokenReturnsFalseForExpiredToken() throws InterruptedException {
        JwtProvider provider = createProvider();
        String token = provider.createJwtToken(99L, 1L);

        Thread.sleep(10L);

        assertFalse(provider.validateToken(token));
    }
}
