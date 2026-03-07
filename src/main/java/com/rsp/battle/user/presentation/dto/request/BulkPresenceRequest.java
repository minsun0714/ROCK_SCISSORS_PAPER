package com.rsp.battle.user.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkPresenceRequest(
        @NotEmpty
        List<Long> userIds
) {
}
