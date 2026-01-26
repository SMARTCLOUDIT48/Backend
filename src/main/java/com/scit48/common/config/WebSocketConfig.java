package com.scit48.common.config;

import com.scit48.auth.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	
	private final JwtProvider jwtProvider;
	
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws/chat")
				.setAllowedOriginPatterns("*")
				.addInterceptors(new HttpHandshakeInterceptor());
		//.withSockJS(); // JS에서 SockJS 사용 시 주석 해제
	}
	
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/sub");
		registry.setApplicationDestinationPrefixes("/pub");
	}
	
	private class HttpHandshakeInterceptor implements HandshakeInterceptor {
		
		@Override
		public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
									   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
			
			if (request instanceof ServletServerHttpRequest) {
				ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
				HttpServletRequest httpRequest = servletRequest.getServletRequest();
				
				Cookie[] cookies = httpRequest.getCookies();
				String token = null;
				
				if (cookies != null) {
					for (Cookie cookie : cookies) {
						if ("accessToken".equals(cookie.getName())) {
							token = cookie.getValue();
							break;
						}
					}
				}
				
				// 🚨 수정된 부분: 메서드 이름과 타입 변경 (validate, getMemberId)
				if (token != null && jwtProvider.validate(token)) {
					
					// 토큰에서 Long 타입의 PK (user_id)를 꺼냅니다.
					Long userId = jwtProvider.getMemberId(token);
					
					// 세션에 저장 (키 이름을 "userId"로 명확하게 변경)
					attributes.put("userId", userId);
					
					System.out.println("✅ 웹소켓 연결 성공! User PK: " + userId);
					return true;
				}
			}
			
			System.out.println("❌ 웹소켓 연결 실패: 유효한 토큰이 없음");
			return false;
		}
		
		@Override
		public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
								   WebSocketHandler wsHandler, Exception exception) {
		}
	}
}