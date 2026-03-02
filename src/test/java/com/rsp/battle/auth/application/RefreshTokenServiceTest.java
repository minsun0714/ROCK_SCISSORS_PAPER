package com.rsp.battle.auth.application;

import com.rsp.battle.auth.domain.AccessToken;
import com.rsp.battle.auth.domain.RefreshToken;
import com.rsp.battle.auth.domain.TokenPolicy;
import com.rsp.battle.auth.infrastructure.JwtProvider;
import com.rsp.battle.auth.infrastructure.RefreshTokenRepository;
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
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenPolicy tokenPolicy;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(
                refreshTokenRepository,
                jwtProvider,
                userRepository,
                tokenPolicy
        );
    }

    @Test
    void reissueAccessTokenReturnsTokenWhenRefreshTokenIsValid() {
        RefreshToken refreshToken = new RefreshToken("refresh-token");
        when(refreshTokenRepository.findUserId(refreshToken)).thenReturn(Optional.of("7"));
        when(userRepository.existsById(7L)).thenReturn(true);
        when(tokenPolicy.accessTokenExpiresInMillis()).thenReturn(1000L * 60 * 30);
        when(jwtProvider.issueAccessToken(anyLong(), anyLong()))
                .thenReturn(new AccessToken("new-access-token", 1000L * 60 * 30));

        Optional<AccessToken> result = refreshTokenService.reissueAccessToken(refreshToken);

        assertTrue(result.isPresent());
        assertEquals("new-access-token", result.get().value());
        assertEquals(1000L * 60 * 30, result.get().expiresInMillis());
        verify(jwtProvider).issueAccessToken(7L, 1000L * 60 * 30);
        verify(refreshTokenRepository, never()).delete(refreshToken);
    }

    @Test
    void reissueAccessTokenReturnsEmptyWhenRefreshTokenIsMissingInStore() {
        RefreshToken refreshToken = new RefreshToken("refresh-token");
        when(refreshTokenRepository.findUserId(refreshToken)).thenReturn(Optional.empty());

        Optional<AccessToken> result = refreshTokenService.reissueAccessToken(refreshToken);

        assertTrue(result.isEmpty());
        verify(jwtProvider, never()).issueAccessToken(anyLong(), anyLong());
    }

    @Test
    void reissueAccessTokenReturnsEmptyAndDeletesRefreshTokenWhenUserMissing() {
        RefreshToken refreshToken = new RefreshToken("refresh-token");
        when(refreshTokenRepository.findUserId(refreshToken)).thenReturn(Optional.of("99"));
        when(userRepository.existsById(99L)).thenReturn(false);

        Optional<AccessToken> result = refreshTokenService.reissueAccessToken(refreshToken);

        assertTrue(result.isEmpty());
        verify(refreshTokenRepository).delete(refreshToken);
        verify(jwtProvider, never()).issueAccessToken(anyLong(), anyLong());
    }
}
