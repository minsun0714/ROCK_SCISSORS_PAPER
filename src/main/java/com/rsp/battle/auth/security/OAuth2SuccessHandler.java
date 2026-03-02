package com.rsp.battle.auth.security;

import com.rsp.battle.auth.application.RefreshTokenService;
import com.rsp.battle.auth.domain.CustomOAuth2User;
import com.rsp.battle.auth.domain.RefreshToken;
import com.rsp.battle.auth.domain.TokenPolicy;
import com.rsp.battle.auth.infrastructure.AuthorizationCodeRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site}")
    private String cookieSameSite;

    @Value("${frontend.url}")
    private String frontendUrl;

    private final RefreshTokenService refreshTokenService;
    private final TokenPolicy tokenPolicy;
    private final AuthorizationCodeRepository authorizationCodeRepository;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        long userId = oAuth2User.getUser().getId();

        RefreshToken refreshToken = new RefreshToken(UUID.randomUUID().toString());

        addCookie(
                response,
                "refresh_token",
                refreshToken.value(),
                tokenPolicy.refreshTokenExpiresInMillis(),
                "/auth/refresh"
        );

        refreshTokenService.save(
                refreshToken,
                userId,
                tokenPolicy.refreshTokenExpiresInMillis()
        );

        String code = UUID.randomUUID().toString();
        authorizationCodeRepository.save(code, userId);

        String targetUrl = UriComponentsBuilder
                .fromUriString(frontendUrl)
                .path("/oauth/callback")
                .queryParam("code", code)
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();

        log.info("oauth callback redirect: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private void addCookie(
            HttpServletResponse response,
            String name,
            String value,
            long expireMillis,
            String path
    ) {
        long maxAge = expireMillis / 1000;

        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path(path)
                .maxAge(maxAge)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}
