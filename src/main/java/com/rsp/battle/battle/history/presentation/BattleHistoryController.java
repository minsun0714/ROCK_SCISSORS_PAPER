package com.rsp.battle.battle.history.presentation;

import com.rsp.battle.auth.domain.CustomUserPrincipal;
import com.rsp.battle.battle.history.application.BattleRoundQueryService;
import com.rsp.battle.user.presentation.dto.response.Paginated;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/battles")
@RequiredArgsConstructor
public class BattleHistoryController {

    private final BattleRoundQueryService battleRoundQueryService;

    @GetMapping("/stats/me")
    public ResponseEntity<BattleRoundStatResponse> getMyBattleStat(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ){
        BattleRoundStatResponse response = battleRoundQueryService.getBattleStat(principal.getUserId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/{userId}")
    public ResponseEntity<BattleRoundStatResponse> getOtherBattleStat(
            @PathVariable Long userId
    ){
        BattleRoundStatResponse response = battleRoundQueryService.getBattleStat(userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/me")
    public ResponseEntity<Paginated<BattleRoundHistoryResponse>> getMyBattleHistory(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false) BattleResult battleResult,
            Pageable pageable
    ) {
        Paginated<BattleRoundHistoryResponse> response = battleRoundQueryService.getBattleHistory(
                principal.getUserId(),
                keyword,
                battleResult,
                pageable
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<Paginated<BattleRoundHistoryResponse>> getOtherBattleHistory(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false) BattleResult battleResult,
            Pageable pageable
    ) {
        Paginated<BattleRoundHistoryResponse> response = battleRoundQueryService.getBattleHistory(
                userId,
                keyword,
                battleResult,
                pageable
        );

        return ResponseEntity.ok(response);
    }
}
