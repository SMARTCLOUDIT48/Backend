package com.scit48.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
	
	/**
	 * 1. 기본 템플릿 (String, String)
	 * - 용도: 단순 텍스트, 인원수 카운팅, 로그아웃 토큰
	 */
	@Bean
	public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory);
		
		// 일반적인 String 처리는 이걸로 충분
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new StringRedisSerializer());
		
		return redisTemplate;
	}
	
	/**
	 * 2. 객체 저장용 템플릿 (String, Object) - ✅ 여기를 수정함
	 * - 용도: ChatMessageDto 같은 객체를 JSON으로 저장할 때 사용
	 */
	@Bean(name = "redisObjectTemplate") // 이름을 명시해주는 게 안전함
	public RedisTemplate<String, Object> redisObjectTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory);
		
		// Key는 문자열
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		
		// ✅ Value는 JSON (GenericJackson2JsonRedisSerializer 추천)
		// 이 시리얼라이저는 객체의 클래스 타입 정보(@class)까지 JSON에 같이 저장해서
		// 나중에 꺼낼 때 ChatMessageDto로 변환하기가 훨씬 수월합니다.
		redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		
		// ✅ Hash 자료구조를 쓸 때를 대비한 설정 (이게 없으면 깨져 보일 수 있음)
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
		
		return redisTemplate;
	}
}