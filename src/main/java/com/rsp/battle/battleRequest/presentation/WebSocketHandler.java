package com.rsp.battle.battleRequest.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsp.battle.battleRequest.application.BattleRoomManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import lombok.extern.slf4j.Slf4j;

import static com.rsp.battle.battleRequest.presentation.MessageType.*;

@Slf4j
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private final BattleRoomManager manager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 배틀 룸 입장
	public void afterConnectionEstablished(WebSocketSession currentSession) throws Exception {
		log.info("WebSocket connection established: {}", currentSession.getId());

        manager.join(currentSession);
	}

	public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
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

        manager.leave(session);
	}

	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		log.error("WebSocket transport error in session: {}", session.getId(), exception);

        manager.leave(session);
	}
}
