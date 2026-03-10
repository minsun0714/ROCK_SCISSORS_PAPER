package com.rsp.battle.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.rsp.battle.battleRequest.presentation.WebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	@Value("${frontend.url}")
	private String frontendUrl;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry
			.addHandler(signalingSocketHandler(), "/ws")
			.setAllowedOrigins(frontendUrl);
	}

	@Bean
	public WebSocketHandler signalingSocketHandler() {
		return new WebSocketHandler();
	}
}
