package com.rsp.battle.friendRequest.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.NumberFormat;

public record FriendRequest(
        @NotNull
        @NumberFormat
        Long targetUserId
) {
}
