package com.rsp.battle.auth.oauth;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class OAuth2StateRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public OAuth2StateRedisService(
            @Qualifier("oauthRedisTemplate")
            RedisTemplate<String, Object> redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    private static final String PREFIX = "oauth2:state:";
    private static final String REDIRECT_URI_PREFIX = "oauth:redirect:";

    private static final Duration TTL = Duration.ofMinutes(3);

    public void save(String state, OAuth2AuthorizationRequest request) {
        redisTemplate.opsForValue()
                .set(PREFIX + state, request, TTL);
    }

    public OAuth2AuthorizationRequest load(String state) {
        Object value = redisTemplate.opsForValue().get(PREFIX + state);
        if (value == null) return null;
        return (OAuth2AuthorizationRequest) value;
    }

    public void delete(String state) {
        redisTemplate.delete(PREFIX + state);
    }

    public void saveRedirectUri(String state, String redirectUri) {
        if (state == null || redirectUri == null) return;
        redisTemplate.opsForValue().set(REDIRECT_URI_PREFIX + state, redirectUri, TTL);
    }

    public String loadRedirectUri(String state) {
        if (state == null) return null;
        return (String) redisTemplate.opsForValue().get(REDIRECT_URI_PREFIX + state);
    }

    public void deleteRedirectUri(String state) {
        if (state == null) return;
        redisTemplate.delete(REDIRECT_URI_PREFIX + state);
    }
}