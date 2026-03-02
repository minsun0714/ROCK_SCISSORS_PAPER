package com.rsp.battle.user.presentation;

import com.rsp.battle.user.domain.User;

public record UserProfileResponse(
        Long id,
        String nickname,
        String statusMessage
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getNickname(),
                user.getStatusMessage()
        );
    }
}