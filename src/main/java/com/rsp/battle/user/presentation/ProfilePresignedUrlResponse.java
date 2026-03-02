package com.rsp.battle.user.presentation;

public record ProfilePresignedUrlResponse(
        String uploadUrl,
        String key
) {
}
