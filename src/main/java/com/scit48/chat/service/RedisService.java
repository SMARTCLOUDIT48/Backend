package com.scit48.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService {
	
	private final RedisTemplate<String, String> redisTemplate;
	
	// 입장: 인원수 +1
	public void userEnter(String roomId) {
		ValueOperations<String, String> ops = redisTemplate.opsForValue();
		ops.increment("roomCount:" + roomId); // 1 증가
	}
	
	// 퇴장: 인원수 -1
	public void userLeave(String roomId) {
		ValueOperations<String, String> ops = redisTemplate.opsForValue();
		ops.decrement("roomCount:" + roomId); // 1 감소
	}
	
	// 현재 인원수 조회
	public long getUserCount(String roomId) {
		ValueOperations<String, String> ops = redisTemplate.opsForValue();
		String count = ops.get("roomCount:" + roomId);
		return (count != null) ? Long.parseLong(count) : 0;
	}
}