package com.rsp.battle.user.presentation.dto.response;

import com.rsp.battle.user.domain.PresenceStatus;

import java.util.Map;

public record BulkPresenceResponse(
        Map<Long, PresenceStatus> presenceStatuses
) {
}
