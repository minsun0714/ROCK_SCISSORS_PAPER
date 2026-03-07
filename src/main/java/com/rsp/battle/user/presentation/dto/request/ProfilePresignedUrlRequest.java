package com.rsp.battle.user.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ProfilePresignedUrlRequest(
        @NotBlank
        String fileName,

        @NotBlank
        String fileType
) {
}
