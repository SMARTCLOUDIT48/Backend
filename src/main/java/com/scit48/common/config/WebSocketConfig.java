package com.scit48.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// 1. 소켓 연결 주소: ws://localhost:8080/ws/chat
		registry.addEndpoint("/ws/chat")
				.setAllowedOriginPatterns("*"); // ⭐ CORS 허용 (필수)
				//.withSockJS(); // JS에서 SockJS 쓸 때 필요 (테스트 땐 이거 때문에 주소 주의)
	}
	
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// 2. 메시지 받을 때(구독) 경로: /sub/chat/room/1
		registry.enableSimpleBroker("/sub");
		
		// 3. 메시지 보낼 때(발행) 경로: /pub/chat/message
		registry.setApplicationDestinationPrefixes("/pub");
	}
}