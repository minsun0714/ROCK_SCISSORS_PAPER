package com.rsp.battle.user.presentation;

import com.rsp.battle.auth.domain.CustomUserPrincipal;
import com.rsp.battle.user.application.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
@Slf4j
public class MyInfoController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<MyInfoResponse> getMyInfo(
            @AuthenticationPrincipal CustomUserPrincipal user
    ) {
        MyInfoResponse response = userService.getMyInfo(user.getUserId());

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/status-message")
    public ResponseEntity<StatusMessageUpdateResponse> updateStatusMessage(
            @AuthenticationPrincipal CustomUserPrincipal user,
            @Valid @RequestBody StatusMessageUpdateRequest statusMessageUpdateRequest
    ) {

        StatusMessageUpdateResponse response = userService.updateStatusMessage(
                        user.getUserId(),
                        statusMessageUpdateRequest
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/profile-picture")
    public ResponseEntity<ProfilePresignedUrlResponse> createProfilePicture(
            @AuthenticationPrincipal CustomUserPrincipal user,
            @Valid @RequestBody ProfilePresignedUrlRequest profilePresignedUrlRequest
    ) {
        ProfilePresignedUrlResponse response = userService.createProfilePicture(
                        user.getUserId(),
                        profilePresignedUrlRequest
        );

        log.info("presignedUrl 발급: {}", response.uploadUrl());

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/profile-picture")
    public ResponseEntity<Void> updateProfilePictureKey(
            @AuthenticationPrincipal CustomUserPrincipal user,
            @Valid @RequestBody ProfilePictureUpdateRequest profilePictureUpdateRequest
    ) {
        userService.updateProfilePictureKey(
                user.getUserId(),
                profilePictureUpdateRequest
        );

        log.info("key:{} DB에 저장 완료", profilePictureUpdateRequest.key());

        return ResponseEntity.noContent().build();
    }
}