package com.rsp.battle.auth.domain;

public record AccessToken(String value, long expiresInMillis) {

    public AccessToken {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("access token must not be blank");
        }
        if (expiresInMillis <= 0) {
            throw new IllegalArgumentException("access token expiry must be positive");
        }
        value = value.trim();
    }

    public long maxAgeSeconds() {
        return expiresInMillis / 1000;
    }
}
