package com.rsp.battle.auth;

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
    private final OAuth2StateRedisService redisService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        long ACCESS_TOKEN_EXPIRES_IN = 1000L * 60 * 30;
        long REFRESH_TOKEN_EXPIRES_IN = 1000L * 60 * 60 * 24 * 7;

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        long userId = oAuth2User.getUser().getId();

        /**
         * access token은 바디에 저장하여 redirect_uri와 함께 발급
         */

        String accessToken = jwtProvider.createJwtToken(
                userId,
                ACCESS_TOKEN_EXPIRES_IN
        );

        addCookie(response, "access_token", accessToken, ACCESS_TOKEN_EXPIRES_IN, "/");

        /**
         * refresh token은 쿠키에 저장하여 발급
         */

        String refreshToken = UUID.randomUUID().toString();

        addCookie(response, "refresh_token", refreshToken, REFRESH_TOKEN_EXPIRES_IN, "/auth/refresh");

        refreshTokenService.save(refreshToken, userId, REFRESH_TOKEN_EXPIRES_IN);

        String state = request.getParameter("state");
        String appRedirectUri = redisService.loadRedirectUri(state);
        redisService.deleteRedirectUri(state);

        if (!isAuthorizedRedirectUri(appRedirectUri)) {
            appRedirectUri = "/";
        }

        log.info("app_redirect_uri: {}", appRedirectUri);
        String targetUrl = frontendUrl + appRedirectUri;

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private boolean isAuthorizedRedirectUri(String uri) {

        if (uri == null) return false;

        // 외부 도메인 차단
        if (uri.startsWith("http://") || uri.startsWith("https://")) {
            return false;
        }

        // 절대경로만 허용
        return uri.startsWith("/");
    }

    private void addCookie(HttpServletResponse response,
                           String name,
                           String value,
                           long expireMillis,
                           String path) {

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
