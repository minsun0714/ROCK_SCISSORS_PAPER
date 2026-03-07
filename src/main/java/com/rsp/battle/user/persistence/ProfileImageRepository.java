package com.rsp.battle.user.persistence;

import com.rsp.battle.user.presentation.dto.response.ProfilePresignedUrlResponse;

public interface ProfileImageRepository {
    ProfilePresignedUrlResponse createUploadUrl(String originalFileName, String contentType);
}
