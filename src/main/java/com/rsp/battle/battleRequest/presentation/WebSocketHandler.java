package com.rsp.battle.battleRequest.presentation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {

	private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

	// 배틀 룸 입장
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		log.info("WebSocket connection established: {}", session.getId());
	}

	// 배틀 라운드 진행
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		log.info("Received message: {} from session: {}", message.getPayload(), session.getId());
		// Handle incoming messages if needed
	}

	// 배틀 룸 퇴장
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		log.info("WebSocket connection closed: {}, status: {}", session.getId(), status);
		sessions.values().remove(session);
	}

	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		log.error("WebSocket transport error in session: {}", session.getId(), exception);
		sessions.values().remove(session);
	}
}
