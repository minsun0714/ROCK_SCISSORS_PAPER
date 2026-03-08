package com.rsp.battle.user.presentation;

import com.rsp.battle.auth.domain.CustomUserPrincipal;
import com.rsp.battle.user.application.UserSearchService;
import com.rsp.battle.user.presentation.dto.response.Paginated;
import com.rsp.battle.user.presentation.dto.response.UserSearchResponse;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/search")
@RequiredArgsConstructor
@Validated
public class UserSearchController {

    private final UserSearchService userSearchService;

    @GetMapping
    public ResponseEntity<Paginated<UserSearchResponse>> getPaginatedUsers(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(required = false, defaultValue = "") String keyword,
            Pageable pageable // page, size
    ) {
        Paginated<UserSearchResponse> response = userSearchService.searchUsers(principal, keyword, pageable);

        return ResponseEntity.ok(response);
    }
}
