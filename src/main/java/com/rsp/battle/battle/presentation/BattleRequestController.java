package com.rsp.battle.battle.presentation;

import com.rsp.battle.auth.domain.CustomUserPrincipal;
import com.rsp.battle.battle.application.BattleRequestService;
import com.rsp.battle.battle.presentation.dto.request.BattleRequest;
import com.rsp.battle.battle.presentation.dto.response.BattleRequestAcceptResponse;
import com.rsp.battle.battle.presentation.dto.response.BattleRequestRejectResponse;
import com.rsp.battle.battle.presentation.dto.response.BattleRequestResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/battles/requests")
@RequiredArgsConstructor
public class BattleRequestController {

    private final BattleRequestService battleRequestService;

    @PostMapping
    public ResponseEntity<BattleRequestResponse> createBattleRequest(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody BattleRequest battleRequest
    ) {
        BattleRequestResponse response = battleRequestService.createBattleRequest(
                principal.getUserId(),
                battleRequest.targetUserId()
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> cancelBattleRequestBySelf(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long requestId
    ) {
        battleRequestService.cancelBattleRequest(
                principal.getUserId(),
                requestId
        );
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{requestId}/accept")
    public ResponseEntity<BattleRequestAcceptResponse> acceptBattleRequest(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long requestId
    ) {
        BattleRequestAcceptResponse response = battleRequestService.acceptBattleRequest(
                principal.getUserId(),
                requestId
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{requestId}/reject")
    public ResponseEntity<BattleRequestRejectResponse> rejectBattleRequest(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long requestId
    ) {
        BattleRequestRejectResponse response = battleRequestService.rejectBattleRequest(
                principal.getUserId(),
                requestId
        );

        return ResponseEntity.ok(response);
    }
}
