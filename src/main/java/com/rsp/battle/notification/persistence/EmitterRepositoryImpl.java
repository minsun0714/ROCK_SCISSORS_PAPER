package com.rsp.battle.notification.persistence;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class EmitterRepositoryImpl implements EmitterRepository{

    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    @Override
    public void save(Long userId, SseEmitter sseEmitter) {
        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>())
                .add(sseEmitter);
    }

    @Override
    public List<SseEmitter> get(Long userId) {
        return emitters.getOrDefault(userId, List.of());
    }

    @Override
    public void delete(Long userId, SseEmitter sseEmitter) {
        List<SseEmitter> list = emitters.get(userId);
        if (list != null) {
            list.remove(sseEmitter);
        }
    }
}
