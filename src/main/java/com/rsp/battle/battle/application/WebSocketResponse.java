package com.rsp.battle.battle.application;

public record WebSocketResponse(WebSocketMessageType type, Object data) {
    public static WebSocketResponse of(WebSocketMessageType type, Object data) {
        return new WebSocketResponse(type, data);
    }
}