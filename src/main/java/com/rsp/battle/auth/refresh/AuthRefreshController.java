package com.rsp.battle.auth.refresh;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthRefreshController {

    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site}")
    private String cookieSameSite;

    private final AuthRefreshService authRefreshService;

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue(value = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        Optional<String> newAccessToken = authRefreshService.reissueAccessToken(refreshToken);

        if (newAccessToken.isEmpty()) {
            clearCookie(response, "access_token", "/");
            clearCookie(response, "refresh_token", "/auth/refresh");
            return ResponseEntity.status(401).build();
        }

        addCookie(
                response,
                "access_token",
                newAccessToken.get(),
                authRefreshService.getAccessTokenExpiresIn(),
                "/"
        );

        return ResponseEntity.noContent().build();
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

    private void clearCookie(HttpServletResponse response, String name, String path) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path(path)
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
