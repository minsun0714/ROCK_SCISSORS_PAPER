package com.rsp.battle.user.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

public record ProfilePictureUpdateRequest(
        @NotNull
        String key
) {
}
