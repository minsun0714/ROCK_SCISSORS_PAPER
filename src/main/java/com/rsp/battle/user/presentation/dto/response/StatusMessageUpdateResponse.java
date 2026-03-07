package com.rsp.battle.user.presentation.dto.response;

import com.rsp.battle.user.domain.User;

public record StatusMessageUpdateResponse(
        Long id,
        String nickname,
        String statusMessage
) {
    public static StatusMessageUpdateResponse from(User user) {
        return new StatusMessageUpdateResponse(
                user.getId(),
                user.getNickname(),
                user.getStatusMessage()
        );
    }
}