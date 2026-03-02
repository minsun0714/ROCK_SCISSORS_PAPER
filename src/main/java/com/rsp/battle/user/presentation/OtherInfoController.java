package com.rsp.battle.user.presentation;

import com.rsp.battle.auth.domain.CustomUserPrincipal;
import com.rsp.battle.user.application.OtherUserInfoQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class OtherInfoController {

    private final OtherUserInfoQueryService otherUserInfoQueryService;

    @GetMapping("/{targetUserId}")
    public ResponseEntity<OtherInfoResponse> getMyInfo(
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            @Valid @PathVariable Long targetUserId
    ) {
        OtherInfoResponse response = otherUserInfoQueryService.getOtherUserInfo(loginUser, targetUserId);

        return ResponseEntity.ok(response);
    }
}