package com.rsp.battle.auth.refresh;

import com.rsp.battle.auth.jwt.JwtProvider;
import com.rsp.battle.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthRefreshServiceTest {

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private UserRepository userRepository;

    private AuthRefreshService authRefreshService;

    @BeforeEach
    void setUp() {
        authRefreshService = new AuthRefreshService(refreshTokenService, jwtProvider, userRepository);
    }

    @Test
    void reissueAccessTokenReturnsTokenWhenRefreshTokenIsValid() {
        when(refreshTokenService.getUserId("refresh-token")).thenReturn("7");
        when(userRepository.existsById(7L)).thenReturn(true);
        when(jwtProvider.createJwtToken(anyLong(), anyLong())).thenReturn("new-access-token");

        Optional<String> result = authRefreshService.reissueAccessToken("refresh-token");

        assertTrue(result.isPresent());
        assertEquals("new-access-token", result.get());
        verify(jwtProvider).createJwtToken(7L, authRefreshService.getAccessTokenExpiresIn());
        verify(refreshTokenService, never()).delete(anyString());
    }

    @Test
    void reissueAccessTokenReturnsEmptyWhenRefreshTokenIsMissingInStore() {
        when(refreshTokenService.getUserId("refresh-token")).thenReturn(null);

        Optional<String> result = authRefreshService.reissueAccessToken("refresh-token");

        assertTrue(result.isEmpty());
        verify(jwtProvider, never()).createJwtToken(anyLong(), anyLong());
    }

    @Test
    void reissueAccessTokenReturnsEmptyAndDeletesRefreshTokenWhenUserMissing() {
        when(refreshTokenService.getUserId("refresh-token")).thenReturn("99");
        when(userRepository.existsById(99L)).thenReturn(false);

        Optional<String> result = authRefreshService.reissueAccessToken("refresh-token");

        assertTrue(result.isEmpty());
        verify(refreshTokenService).delete("refresh-token");
        verify(jwtProvider, never()).createJwtToken(anyLong(), anyLong());
    }
}
