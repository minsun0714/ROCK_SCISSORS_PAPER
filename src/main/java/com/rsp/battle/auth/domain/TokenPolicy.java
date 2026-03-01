package com.rsp.battle.auth.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TokenPolicy {

    private final long accessTokenExpiresInMillis;
    private final long refreshTokenExpiresInMillis;

    public TokenPolicy(
            @Value("${app.auth.access-token-expire-millis:1800000}") long accessTokenExpiresInMillis,
            @Value("${app.auth.refresh-token-expire-millis:604800000}") long refreshTokenExpiresInMillis
    ) {
        this.accessTokenExpiresInMillis = accessTokenExpiresInMillis;
        this.refreshTokenExpiresInMillis = refreshTokenExpiresInMillis;
    }

    public long accessTokenExpiresInMillis() {
        return accessTokenExpiresInMillis;
    }

    public long refreshTokenExpiresInMillis() {
        return refreshTokenExpiresInMillis;
    }
}
