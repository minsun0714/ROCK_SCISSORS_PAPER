package com.rsp.battle.auth.presentation;

import com.rsp.battle.auth.infrastructure.JwtProvider;
import com.rsp.battle.user.domain.User;
import com.rsp.battle.user.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Profile("loadtest")
@RestController
@RequestMapping("/auth/loadtest")
@RequiredArgsConstructor
public class LoadTestTokenController {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Value("${app.auth.access-token-expire-millis}")
    private long accessTokenExpireMillis;

    @PostMapping("/token")
    public ResponseEntity<Map<String, Object>> issueToken(@RequestParam Long userId) {
        String token = jwtProvider.createJwtToken(userId, accessTokenExpireMillis);
        return ResponseEntity.ok(Map.of(
                "token", token,
                "userId", userId
        ));
    }

    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createTestUser(@RequestParam String name) {
        User user = User.createFromOAuth(name + "@loadtest.com", name, "LOADTEST");
        User saved = userRepository.save(user);
        saved.updateNickname(name + "#" + saved.getId());

        String token = jwtProvider.createJwtToken(saved.getId(), accessTokenExpireMillis);
        return ResponseEntity.ok(Map.of(
                "userId", saved.getId(),
                "nickname", saved.getNickname(),
                "token", token
        ));
    }
}
