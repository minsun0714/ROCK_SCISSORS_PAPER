package com.rsp.battle.auth.infrastructure;

import com.rsp.battle.auth.domain.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository {
    void save(RefreshToken token, Long userId, long expireMillis);
    Optional<String> findUserId(RefreshToken token);
    void delete(RefreshToken token);
}