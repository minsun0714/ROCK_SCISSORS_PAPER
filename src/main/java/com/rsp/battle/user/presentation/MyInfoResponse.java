package com.rsp.battle.user.presentation;

import com.rsp.battle.user.domain.User;

public record MyInfoResponse(
        Long userId,
        String nickname,
        String email,
        String profileImageUrl,
        String statusMessage,
        PresenceStatus presenceStatus
) {
    public static MyInfoResponse from(User user, String profileImageUrl, PresenceStatus presenceStatus) {
        return new MyInfoResponse(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                profileImageUrl,
                user.getStatusMessage(),
                presenceStatus
        );
    }
}
