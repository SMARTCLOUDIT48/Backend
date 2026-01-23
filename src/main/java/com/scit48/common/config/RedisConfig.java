package com.scit48.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
	
	/**
	 * 1. 기본 템플릿 (문자열/숫자용)
	 * - 용도: 인원수 카운팅, 로그아웃 토큰 관리
	 */
	@Bean
	public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory);
		
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new StringRedisSerializer());
		
		return redisTemplate;
	}
	
	/**
	 * 2. 객체 저장용 템플릿 (JSON용)
	 * - 용도: 채팅방 정보 캐싱, 유저 정보 캐싱 (나중을 위해 미리 준비)
	 * - 메서드 이름을 다르게 설정(redisObjectTemplate)
	 */
	@Bean
	public RedisTemplate<String, Object> redisObjectTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory);
		
		// Key는 문자열로 저장
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		
		// Value는 JSON으로 저장 (객체 -> JSON 자동 변환)
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
		
		return redisTemplate;
	}
}