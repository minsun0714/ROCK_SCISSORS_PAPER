package com.rsp.battle.auth.application;

import com.rsp.battle.auth.domain.AccessToken;
import com.rsp.battle.auth.domain.TokenPolicy;
import com.rsp.battle.auth.infrastructure.AuthorizationCodeRepository;
import com.rsp.battle.auth.infrastructure.JwtProvider;
import com.rsp.battle.user.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessTokenServiceTest {

    @Mock
    private AuthorizationCodeRepository authorizationCodeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private TokenPolicy tokenPolicy;

    private AccessTokenService accessTokenService;

    @BeforeEach
    void setUp() {
        accessTokenService = new AccessTokenService(
                authorizationCodeRepository,
                userRepository,
                jwtProvider,
                tokenPolicy
        );
    }

    @Test
    void exchangeReturnsAccessTokenWhenCodeIsValid() {
        when(authorizationCodeRepository.consumeUserId("valid-code")).thenReturn("7");
        when(userRepository.existsById(7L)).thenReturn(true);
        when(tokenPolicy.accessTokenExpiresInMillis()).thenReturn(1_800_000L);
        when(jwtProvider.issueAccessToken(7L, 1_800_000L))
                .thenReturn(new AccessToken("access-token", 1_800_000L));

        Optional<AccessToken> result = accessTokenService.exchange("valid-code");

        assertTrue(result.isPresent());
        assertEquals("access-token", result.get().value());
        assertEquals(1_800_000L, result.get().expiresInMillis());
    }

    @Test
    void exchangeReturnsEmptyWhenCodeIsMissing() {
        when(authorizationCodeRepository.consumeUserId("missing-code")).thenReturn(null);

        Optional<AccessToken> result = accessTokenService.exchange("missing-code");

        assertTrue(result.isEmpty());
        verify(jwtProvider, never()).issueAccessToken(anyLong(), anyLong());
    }

    @Test
    void exchangeReturnsEmptyWhenUserIdIsNotNumeric() {
        when(authorizationCodeRepository.consumeUserId("bad-code")).thenReturn("not-number");

        Optional<AccessToken> result = accessTokenService.exchange("bad-code");

        assertTrue(result.isEmpty());
        verify(jwtProvider, never()).issueAccessToken(anyLong(), anyLong());
    }

    @Test
    void exchangeReturnsEmptyWhenUserDoesNotExist() {
        when(authorizationCodeRepository.consumeUserId("valid-code")).thenReturn("9");
        when(userRepository.existsById(9L)).thenReturn(false);

        Optional<AccessToken> result = accessTokenService.exchange("valid-code");

        assertTrue(result.isEmpty());
        verify(jwtProvider, never()).issueAccessToken(anyLong(), anyLong());
    }
}
