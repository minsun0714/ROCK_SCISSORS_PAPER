package com.rsp.battle.battleRequest.presentation;

import com.rsp.battle.auth.domain.CustomUserPrincipal;
import com.rsp.battle.auth.infrastructure.JwtProvider;
import com.rsp.battle.battleRequest.domain.BattleRoom;
import com.rsp.battle.battleRequest.persistence.BattleRoomRepository;
import com.rsp.battle.common.exception.BusinessException;
import com.rsp.battle.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketInterceptor implements HandshakeInterceptor {

    private final BattleRoomRepository battleRoomRepository;
    private final JwtProvider jwtProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String tokenParam = ((ServletServerHttpRequest) request)
                .getServletRequest()
                .getParameter("token");

        String roomIdParam = ((ServletServerHttpRequest) request)
                .getServletRequest()
                .getParameter("roomId");

        if (tokenParam == null || !jwtProvider.validateToken(tokenParam) || roomIdParam == null) {
            return false;
        }

        Authentication authentication =
                jwtProvider.getAuthentication(tokenParam);

        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();

        Long userId = principal.getUserId();
        Long roomId = Long.valueOf(roomIdParam);

        BattleRoom battleRoom = battleRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));
        if (!Objects.equals(battleRoom.getOpponent(), userId)
                && !Objects.equals(battleRoom.getRequester(), userId)
        ) {
            return false;
        }

        attributes.put("userId", userId);
        attributes.put("roomId", roomId);

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        log.info("handshake 성공");
    }
}
