package com.scit48.common.config;

import com.scit48.auth.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	
	private final JwtProvider jwtProvider;
	
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws/chat") // 엔드포인트 유지
				.setAllowedOriginPatterns("*")
				.addInterceptors(new HttpHandshakeInterceptor()) // 인터셉터 등록
				.withSockJS(); // 👈 JS 호환성을 위해 SockJS 활성화 추천
	}
	
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.enableSimpleBroker("/sub");
		registry.setApplicationDestinationPrefixes("/pub");
	}
	
	// 🌟 핸드셰이크 인터셉터 (연결 요청 시 토큰 검사)
	private class HttpHandshakeInterceptor implements HandshakeInterceptor {
		
		@Override
		public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
									   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
			
			if (request instanceof ServletServerHttpRequest) {
				ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
				HttpServletRequest httpRequest = servletRequest.getServletRequest();
				
				String token = null;
				
				// ----------------------------------------------------
				// 1순위: 쿼리 파라미터 확인 (?token=eyJ...)
				// ----------------------------------------------------
				if (request.getURI().getQuery() != null) {
					String query = request.getURI().getQuery();
					// "token=" 파싱 로직
					String[] params = query.split("&");
					for (String param : params) {
						if (param.startsWith("token=")) {
							token = param.substring(6); // "token=" 이후 문자열
							break;
						}
					}
				}
				
				// ----------------------------------------------------
				// 2순위: 헤더 확인 (Authorization: Bearer ...)
				// ----------------------------------------------------
				if (token == null) {
					String header = httpRequest.getHeader("Authorization");
					if (header != null && header.startsWith("Bearer ")) {
						token = header.substring(7);
					}
				}
				
				// ----------------------------------------------------
				// 3순위: 쿠키 확인 (기존 로직 유지)
				// ----------------------------------------------------
				if (token == null) {
					Cookie[] cookies = httpRequest.getCookies();
					if (cookies != null) {
						for (Cookie cookie : cookies) {
							if ("accessToken".equals(cookie.getName())) {
								token = cookie.getValue();
								break;
							}
						}
					}
				}
				
				// ----------------------------------------------------
				// 최종 검증
				// ----------------------------------------------------
				if (token != null && jwtProvider.validate(token)) {
					
					// 토큰에서 정보 추출 (메서드명 확인 필요)
					Long userId = jwtProvider.getMemberId(token); // PK
					// String nickname = jwtProvider.getNickname(token); // 필요 시 추가
					
					// 세션에 저장 -> Controller에서 사용 가능
					attributes.put("userId", userId);
					
					log.info("✅ 웹소켓 연결 성공! User PK: {}", userId);
					return true;
				}
			}
			
			log.error("❌ 웹소켓 연결 실패: 유효한 토큰을 찾을 수 없음");
			return false; // 연결 거부
		}
		
		@Override
		public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
								   WebSocketHandler wsHandler, Exception exception) {
		}
	}
}