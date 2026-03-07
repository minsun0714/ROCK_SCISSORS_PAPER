package com.rsp.battle.user.presentation.dto.response;

public record ProfilePresignedUrlResponse(
        String uploadUrl,
        String key
) {
}
