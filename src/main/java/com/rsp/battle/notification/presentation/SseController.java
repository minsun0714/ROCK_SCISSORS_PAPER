package com.rsp.battle.notification.presentation;

import com.rsp.battle.auth.domain.CustomUserPrincipal;
import com.rsp.battle.notification.application.SseService;
import com.rsp.battle.notification.presentation.dto.SseSend;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class SseController {
    private final SseService sseService;

    @GetMapping("/subscribe")
    public SseEmitter subscribe(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ){
        return sseService.subscribe(principal.getUserId());
    }

    @PostMapping("/send")
    public void sendAlarm(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody SseSend sseSend
    ) {
        sseService.sendToClient(principal.getUserId(), sseSend.eventName().name(), sseSend.data());
    }
}