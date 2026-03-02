package com.rsp.battle.auth.application;

import com.rsp.battle.auth.domain.AccessToken;
import com.rsp.battle.auth.infrastructure.JwtProvider;
import com.rsp.battle.auth.domain.TokenPolicy;
import com.rsp.battle.auth.domain.RefreshToken;
import com.rsp.battle.auth.infrastructure.RefreshTokenRepository;
import com.rsp.battle.user.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final TokenPolicy tokenPolicy;

    public void save(RefreshToken refreshToken, Long userId, long expireMillis) {
        refreshTokenRepository.save(refreshToken, userId, expireMillis);
    }

    public Optional<String> getUserId(RefreshToken refreshToken) {
        return refreshTokenRepository.findUserId(refreshToken);
    }

    public void delete(RefreshToken refreshToken) {
        refreshTokenRepository.delete(refreshToken);
    }

    public Optional<AccessToken> reissueAccessToken(RefreshToken refreshToken) {
        Optional<String> userIdValue = getUserId(refreshToken);
        if (userIdValue.isEmpty()) {
            return Optional.empty();
        }

        long userId;
        try {
            userId = Long.parseLong(userIdValue.get());
        } catch (NumberFormatException e) {
            return Optional.empty();
        }

        if (!userRepository.existsById(userId)) {
            delete(refreshToken);
            return Optional.empty();
        }

        AccessToken accessToken = jwtProvider.issueAccessToken(
                userId,
                tokenPolicy.accessTokenExpiresInMillis()
        );
        return Optional.of(accessToken);
    }
}
