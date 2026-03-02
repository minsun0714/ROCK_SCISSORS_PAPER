package com.rsp.battle.auth.presentation;

import com.rsp.battle.auth.application.AccessTokenService;
import com.rsp.battle.auth.domain.AccessToken;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AccessTokenController {

    private final AccessTokenService accessTokenService;

    @GetMapping("/exchange")
    public ResponseEntity<Void> exchange(
            @RequestParam String code,
            HttpServletResponse response
    ) {
        Optional<AccessToken> accessToken = accessTokenService.exchange(code);
        if (accessToken.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        response.setHeader("Authorization", "Bearer " + accessToken.get().value());
        return ResponseEntity.noContent().build();
    }
}
