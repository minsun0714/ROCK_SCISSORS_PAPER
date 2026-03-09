package com.rsp.battle.notification.application;

import com.rsp.battle.battleRequest.domain.event.BattleRequestEvent;
import com.rsp.battle.battleRequest.domain.event.BattleRequestNotificationData;
import com.rsp.battle.common.exception.BusinessException;
import com.rsp.battle.common.exception.ErrorCode;
import com.rsp.battle.user.domain.User;
import com.rsp.battle.user.persistence.UserRepository;
import com.rsp.battle.user.presentation.ProfileImageUrlResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class BattleRequestEventListener {

    private final SseService sseService;
    private final UserRepository userRepository;
    private final ProfileImageUrlResolver profileImageUrlResolver;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendNotification(BattleRequestEvent event) {
        try {
            User sender = userRepository.findById(event.senderId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            BattleRequestNotificationData data = new BattleRequestNotificationData(
                    sender.getId(),
                    sender.getNickname(),
                    profileImageUrlResolver.resolve(sender.getProfileImageKey())
            );

            sseService.sendToClient(event.receiverId(), event.type().name(), data);
        } catch (Exception e) {
            log.warn("SSE 알림 전송 실패 (receiver={}): {}", event.receiverId(), e.getMessage());
        }
    }
}
