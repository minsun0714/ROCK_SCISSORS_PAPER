package com.rsp.battle.friend.presentation;

import com.rsp.battle.auth.domain.CustomUserPrincipal;
import com.rsp.battle.friend.application.FriendQueryService;
import com.rsp.battle.user.presentation.dto.response.Paginated;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendQueryService friendQueryService;

    @GetMapping("/me")
    public ResponseEntity<Paginated<FriendResponse>> getMyPaginatedFriendList(
                @AuthenticationPrincipal CustomUserPrincipal principal,
                @RequestParam(required = false, defaultValue = "") String keyword,
                Pageable pageable
    ) {
            Paginated<FriendResponse> response = friendQueryService.getMyPaginatedFriends(
                    principal.getUserId(),
                    keyword,
                    pageable
            );

            return ResponseEntity.ok(response);
    }

    @GetMapping("/{otherUserId}")
    public ResponseEntity<Paginated<FriendResponse>> getOtherUserPaginatedFriends(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long otherUserId,
            @RequestParam(required = false, defaultValue = "") String keyword,
            Pageable pageable
    ) {
        Paginated<FriendResponse> response = friendQueryService.getOtherUserPaginatedFriends(
                principal,
                otherUserId,
                keyword,
                pageable
        );

        return ResponseEntity.ok(response);
    }
}
