package com.rsp.battle.notification.persistence;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface EmitterRepository {
    void save(Long userId, SseEmitter sseEmitter);
    List<SseEmitter> get(Long userId);
    void delete(Long userId, SseEmitter sseEmitter);
}
