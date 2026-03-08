package com.rsp.battle.notification.application;

import com.rsp.battle.friendRequest.domain.event.FriendRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class FriendRequestEventListener {

    private final SseService sseService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendNotification(FriendRequestEvent event) {
        log.info("이벤트 수신: receiverId={}, type={}, data={}", event.receiverId(), event.type(), event.data());
        sseService.sendToClient(event.receiverId(), event.type().name(), event.data());
    }
}
