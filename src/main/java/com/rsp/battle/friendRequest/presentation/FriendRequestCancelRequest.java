package com.rsp.battle.friendRequest.presentation;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.NumberFormat;

public record FriendRequestCancelRequest(
        @NotNull
        @NumberFormat
        Long requestId
) {
}
