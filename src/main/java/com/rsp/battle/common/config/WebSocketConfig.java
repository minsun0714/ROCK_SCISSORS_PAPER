package com.rsp.battle.common.config;

import com.rsp.battle.battleRequest.application.BattleRoomManager;
import com.rsp.battle.battleRequest.presentation.WebSocketInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.rsp.battle.battleRequest.presentation.WebSocketHandler;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

	@Value("${frontend.url}")
	private String frontendUrl;

    private final BattleRoomManager battleRoomManager;
    private final WebSocketInterceptor webSocketInterceptor;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry
			.addHandler(signalingSocketHandler(), "/ws")
                .addInterceptors(webSocketInterceptor)
			.setAllowedOrigins(frontendUrl);
	}

	@Bean
	public WebSocketHandler signalingSocketHandler() {
		return new WebSocketHandler(battleRoomManager);
	}
}
