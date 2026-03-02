package com.rsp.battle.auth.application;

import com.rsp.battle.auth.domain.AccessToken;
import com.rsp.battle.auth.domain.TokenPolicy;
import com.rsp.battle.auth.infrastructure.AuthorizationCodeRepository;
import com.rsp.battle.auth.infrastructure.JwtProvider;
import com.rsp.battle.user.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccessTokenService {

    private final AuthorizationCodeRepository authorizationCodeRepository;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final TokenPolicy tokenPolicy;

    public Optional<AccessToken> exchange(String code) {
        String userIdValue = authorizationCodeRepository.consumeUserId(code);
        if (userIdValue == null) {
            return Optional.empty();
        }

        long userId;
        try {
            userId = Long.parseLong(userIdValue);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }

        if (!userRepository.existsById(userId)) {
            return Optional.empty();
        }

        AccessToken accessToken = jwtProvider.issueAccessToken(
                userId,
                tokenPolicy.accessTokenExpiresInMillis()
        );

        return Optional.of(accessToken);
    }
}
