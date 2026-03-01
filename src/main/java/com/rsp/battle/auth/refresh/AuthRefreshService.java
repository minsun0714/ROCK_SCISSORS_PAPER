package com.rsp.battle.auth.refresh;

import com.rsp.battle.auth.jwt.JwtProvider;
import com.rsp.battle.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthRefreshService {

    private static final long ACCESS_TOKEN_EXPIRES_IN = 1000L * 60 * 30;

    private final RefreshTokenService refreshTokenService;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    public Optional<String> reissueAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return Optional.empty();
        }

        String userIdValue = refreshTokenService.getUserId(refreshToken);
        if (userIdValue == null || userIdValue.isBlank()) {
            return Optional.empty();
        }

        long userId;
        try {
            userId = Long.parseLong(userIdValue);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }

        if (!userRepository.existsById(userId)) {
            refreshTokenService.delete(refreshToken);
            return Optional.empty();
        }

        String accessToken = jwtProvider.createJwtToken(userId, ACCESS_TOKEN_EXPIRES_IN);
        return Optional.of(accessToken);
    }

    public long getAccessTokenExpiresIn() {
        return ACCESS_TOKEN_EXPIRES_IN;
    }
}