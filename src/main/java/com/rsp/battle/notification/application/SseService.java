package com.rsp.battle.notification.application;

import com.rsp.battle.notification.persistence.EmitterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseService {

    private final EmitterRepository emitterRepository;

    public SseEmitter subscribe(Long receiverId) {
        long TIMEOUT = 1000L * 60 * 60;

        SseEmitter sseEmitter = new SseEmitter(TIMEOUT);

        emitterRepository.save(receiverId, sseEmitter);

        sseEmitter.onCompletion(() -> emitterRepository.delete(receiverId, sseEmitter));
        sseEmitter.onTimeout(sseEmitter::complete);
        sseEmitter.onError(e -> {
            log.warn("SSE error for user {}: {}", receiverId, e.getMessage());
            emitterRepository.delete(receiverId, sseEmitter);
        });

        try {
            // 구독 시 더미 이벤트 발생시켜 비정상적인 emitter의 경우 삭제시킴
            sseEmitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            emitterRepository.delete(receiverId, sseEmitter);
        }

        return sseEmitter;
    }

    public void sendToClient(Long receiverId, String eventName, Object data) {
        List<SseEmitter> sseEmitterList = emitterRepository.get(receiverId);
        log.info("SSE send to user {}: event={}, emitter count={}", receiverId, eventName, sseEmitterList.size());

        for (SseEmitter sseEmitter: sseEmitterList) {
            try {
                sseEmitter.send(
                        SseEmitter
                                .event()
                                .id(String.valueOf(receiverId))
                                .name(eventName)
                                .data(data)
                );
            } catch (IOException e) {
                log.warn("SSE send failed for user {}, removing emitter: {}", receiverId, e.getMessage());
                emitterRepository.delete(receiverId, sseEmitter);
            }
        }
    }
}
