package com.rsp.battle.battleRequest.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.NumberFormat;

public record BattleRequest(
        @NotNull
        @NumberFormat
        Long targetUserId
) {
}
