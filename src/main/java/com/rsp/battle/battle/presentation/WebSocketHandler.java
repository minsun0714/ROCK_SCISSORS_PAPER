package com.rsp.battle.battle.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsp.battle.battle.application.BattleRoomManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import lombok.extern.slf4j.Slf4j;

import static com.rsp.battle.battle.presentation.MessageType.*;

@Slf4j
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private final BattleRoomManager manager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 배틀 룸 입장
	public void afterConnectionEstablished(WebSocketSession currentSession) throws Exception {
		log.info("WebSocket connection established: {}", currentSession.getId());

        manager.join(currentSession);
        log.info("join : {}", currentSession.getAttributes().get("userId"));
	}

	public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("message: {}", message);
		log.info("Received message: {} from session: {}", message.getPayload(), session.getId());

        JsonNode json = objectMapper.readTree(message.getPayload());
        String type = json.get("type").asText();

        Long roomId = (Long) session.getAttributes().get("roomId");

        switch (type) {
            case RETRY -> manager.startBattle(roomId);
            case CHOICE -> manager.playBattle(roomId, session, json.get("move").asText());
            default -> session.sendMessage(new TextMessage("메시지 타입이 잘못되었습니다."));
        }
	}

	// 배틀 룸 퇴장
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		log.info("WebSocket connection closed: {}, status: {}", session.getId(), status);
        log.info("message {}", status.getReason());
        manager.leave(session);
	}

	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		log.error("WebSocket transport error in session: {}", session.getId(), exception);
        log.error("error message: {} {}", exception.fillInStackTrace(), exception.getStackTrace());

        manager.leave(session);
	}
}
