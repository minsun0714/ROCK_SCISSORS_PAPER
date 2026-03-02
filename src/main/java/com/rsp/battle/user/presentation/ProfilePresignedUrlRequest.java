package com.rsp.battle.user.presentation;

import jakarta.validation.constraints.NotBlank;

public record ProfilePresignedUrlRequest(
        @NotBlank
        String fileName,

        @NotBlank
        String fileType
) {
}
