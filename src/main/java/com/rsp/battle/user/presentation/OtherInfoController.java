package com.rsp.battle.user.presentation;

import com.rsp.battle.auth.domain.CustomUserPrincipal;
import com.rsp.battle.user.application.OtherUserInfoQueryService;
import com.rsp.battle.user.application.UserService;
import com.rsp.battle.user.domain.PresenceStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class OtherInfoController {

    private final OtherUserInfoQueryService otherUserInfoQueryService;
    private final UserService userService;

    @PostMapping("/presence")
    public ResponseEntity<BulkPresenceResponse> getPresenceStatuses(
            @Valid @RequestBody BulkPresenceRequest request
    ) {
        Map<Long, PresenceStatus> presenceStatuses = userService.getPresenceStatuses(request.userIds());
        return ResponseEntity.ok(new BulkPresenceResponse(presenceStatuses));
    }

    @GetMapping("/{targetUserId}")
    public ResponseEntity<OtherInfoResponse> getMyInfo(
            @AuthenticationPrincipal CustomUserPrincipal loginUser,
            @Valid @PathVariable Long targetUserId
    ) {
        OtherInfoResponse response = otherUserInfoQueryService.getOtherUserInfo(loginUser, targetUserId);

        return ResponseEntity.ok(response);
    }
}