package com.rsp.battle.user.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

public record StatusMessageUpdateRequest(
        @NotNull
        String statusMessage
) {
}
