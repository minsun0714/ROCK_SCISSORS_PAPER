package com.rsp.battle.auth.domain;

import java.util.Optional;

public record RefreshToken(String value) {

    private static final String REDIS_KEY_PREFIX = "refresh:";

    public RefreshToken {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("refresh token must not be blank");
        }
        value = value.trim();
    }

    public static Optional<RefreshToken> fromNullable(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new RefreshToken(value));
    }

    public String redisKey() {
        return REDIS_KEY_PREFIX + value;
    }
}
