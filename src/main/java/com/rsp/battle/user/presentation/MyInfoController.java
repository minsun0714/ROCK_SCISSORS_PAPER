package com.rsp.battle.user.presentation;

import com.rsp.battle.auth.domain.CustomUserPrincipal;
import com.rsp.battle.user.application.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
@Slf4j
public class MyInfoController {

    private final UserService userService;

    @PatchMapping("/status-message")
    public ResponseEntity<UserProfileResponse> updateStatusMessage(
            @AuthenticationPrincipal CustomUserPrincipal user,
            @RequestBody UserProfileRequest userProfileRequest
    ) {

        UserProfileResponse response = userService.updateStatusMessage(
                        user.getUserId(),
                        userProfileRequest
                );

        return ResponseEntity.ok(response);
    }
}