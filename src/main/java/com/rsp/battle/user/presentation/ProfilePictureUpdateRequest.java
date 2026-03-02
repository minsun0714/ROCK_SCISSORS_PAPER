package com.rsp.battle.user.presentation;

import jakarta.validation.constraints.NotNull;

public record ProfilePictureUpdateRequest(
        @NotNull
        String key
) {
}
