package com.rsp.battle.auth.infrastructure;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site}")
    private String cookieSameSite;

    private static final String STATE_COOKIE_NAME = "oauth2_state";

    private final OAuth2StateRepository oAuth2StateRepository;

    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        String state = getStateFromCookie(request);
        if (state == null) return null;

        return oAuth2StateRepository.load(state);
    }

    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        if (authorizationRequest == null) {
            removeStateCookie(response);
            return;
        }

        String state = authorizationRequest.getState();
        oAuth2StateRepository.save(state, authorizationRequest);

        addStateCookie(response, state);
    }

    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String state = getStateFromCookie(request);
        if (state == null) return null;

        OAuth2AuthorizationRequest requestToReturn = oAuth2StateRepository.load(state);
        oAuth2StateRepository.delete(state);
        removeStateCookie(response);

        return requestToReturn;
    }

    private void addStateCookie(HttpServletResponse response, String state) {
        ResponseCookie cookie = ResponseCookie.from(STATE_COOKIE_NAME, state)
                .path("/")
                .httpOnly(true)
                .maxAge(180)
                .sameSite(cookieSameSite)
                .secure(cookieSecure)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void removeStateCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(STATE_COOKIE_NAME)
                .path("/")
                .maxAge(0)
                .sameSite(cookieSameSite)
                .secure(cookieSecure)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    private String getStateFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if (STATE_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
