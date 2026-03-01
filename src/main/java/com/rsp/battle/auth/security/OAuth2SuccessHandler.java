package com.rsp.battle.auth.security;

import com.rsp.battle.auth.application.RefreshTokenService;
import com.rsp.battle.auth.domain.AccessToken;
import com.rsp.battle.auth.domain.CustomOAuth2User;
import com.rsp.battle.auth.domain.RefreshToken;
import com.rsp.battle.auth.domain.TokenPolicy;
import com.rsp.battle.auth.infrastructure.JwtProvider;
import com.rsp.battle.auth.infrastructure.OAuth2StateRepository;
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

import java.io.IOException;
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

    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final TokenPolicy tokenPolicy;
    private final OAuth2StateRepository oAuth2StateRepository;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        long userId = oAuth2User.getUser().getId();

        AccessToken accessToken = jwtProvider.issueAccessToken(
                userId,
                tokenPolicy.accessTokenExpiresInMillis()
        );

        addCookie(
                response,
                "access_token",
                accessToken.value(),
                accessToken.expiresInMillis(),
                "/"
        );

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

        String state = request.getParameter("state");
        String appRedirectUri = oAuth2StateRepository.loadRedirectUri(state);
        oAuth2StateRepository.deleteRedirectUri(state);

        if (!isAuthorizedRedirectUri(appRedirectUri)) {
            appRedirectUri = "/";
        }

        log.info("app_redirect_uri: {}", appRedirectUri);
        String targetUrl = frontendUrl + appRedirectUri;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        if (uri == null) return false;

        if (uri.startsWith("http://") || uri.startsWith("https://")) {
            return false;
        }

        return uri.startsWith("/");
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
