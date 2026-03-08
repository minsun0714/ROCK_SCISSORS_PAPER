package com.rsp.battle.friendRequest.presentation;

import com.rsp.battle.auth.domain.CustomUserPrincipal;
import com.rsp.battle.friend.presentation.FriendResponse;
import com.rsp.battle.friendRequest.application.FriendRequestQueryService;
import com.rsp.battle.friendRequest.application.FriendRequestService;
import com.rsp.battle.friendRequest.presentation.dto.request.FriendRequest;
import com.rsp.battle.friendRequest.presentation.dto.response.FriendRequestResponse;
import com.rsp.battle.user.presentation.dto.response.Paginated;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/friends/requests")
@RequiredArgsConstructor
public class FriendRequestController {

    private final FriendRequestService friendRequestService;
    private final FriendRequestQueryService friendRequestQueryService;

    @GetMapping("/pending")
    public ResponseEntity<Paginated<FriendResponse>> getMyPaginatedPendingUserList(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(required = false, defaultValue = "") String keyword,
            Pageable pageable
    ) {
        Paginated<FriendResponse> response = friendRequestQueryService.getMyPaginatedPendingUserList(
                principal.getUserId(),
                keyword,
                pageable
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/requested")
    public ResponseEntity<Paginated<FriendResponse>> getMyPaginatedRequestedUserList(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(required = false, defaultValue = "") String keyword,
            Pageable pageable
    ) {
        Paginated<FriendResponse> response = friendRequestQueryService.getMyPaginatedRequestedUserList(
                principal.getUserId(),
                keyword,
                pageable
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<FriendRequestResponse> createFriendRequest(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody FriendRequest friendRequest
    ) {
        FriendRequestResponse response = friendRequestService.createFriendRequest(
                principal.getUserId(),
                friendRequest.targetUserId()
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> cancelFriendRequestBySelf(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long requestId
    ) {
        friendRequestService.cancelFriendRequest(
                principal.getUserId(),
                requestId
        );
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{requestId}/accept")
    public ResponseEntity<FriendRequestAcceptResponse> acceptFriendRequest(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @PathVariable Long requestId
    ) {
        FriendRequestAcceptResponse response = friendRequestService.acceptFriendRequest(
                principal.getUserId(),
                requestId
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{requestId}/reject")
    public ResponseEntity<FriendRequestRejectResponse> rejectFriendRequest(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @PathVariable Long requestId
    ) {
        FriendRequestRejectResponse response = friendRequestService.rejectFriendRequest(
                principal.getUserId(),
                requestId
        );

        return ResponseEntity.ok(response);
    }
}
