package com.scit48.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {
	
	// <String, String> 타입을 그대로 사용합니다.
	private final RedisTemplate<String, String> redisTemplate;
	
	// ==========================================
	// 1. 기존 기능: 채팅방 현재 인원 수 관리 (동시 접속자)
	// ==========================================
	
	// 입장: 인원수 +1
	public void userEnter(String roomId) {
		ValueOperations<String, String> ops = redisTemplate.opsForValue();
		ops.increment("roomCount:" + roomId);
	}
	
	// 퇴장: 인원수 -1
	public void userLeave(String roomId) {
		ValueOperations<String, String> ops = redisTemplate.opsForValue();
		// 0보다 작아지지 않도록 방어 로직 살짝 추가하면 더 좋습니다
		ops.decrement("roomCount:" + roomId);
	}
	
	// 현재 인원수 조회
	public long getUserCount(String roomId) {
		ValueOperations<String, String> ops = redisTemplate.opsForValue();
		String count = ops.get("roomCount:" + roomId);
		return (count != null) ? Long.parseLong(count) : 0;
	}
	
	// ==========================================
	// 2. 추가 기능: 오늘 대화한 사람 수 집계 (활동량)
	// ==========================================
	
	// 대화 발생 시 기록 (중복 자동 제거됨)
	public void recordInteraction(Long myId, Long partnerId) {
		// Redis Key: daily:interaction:내ID:오늘날짜
		String key = "daily:interaction:" + myId + ":" + LocalDate.now();
		
		// opsForSet()을 사용해야 중복된 사람을 카운트하지 않습니다.
		// 기존 템플릿이 <String, String>이므로 숫자를 String으로 변환해서 넣습니다.
		redisTemplate.opsForSet().add(key, String.valueOf(partnerId));
		
		// 하루(24시간) 뒤에 기록 자동 삭제 (메모리 관리)
		redisTemplate.expire(key, 1, TimeUnit.DAYS);
	}
	
	// 오늘 몇 명이랑 대화했는지 조회
	public Long getTodayInteractionCount(Long myId) {
		String key = "daily:interaction:" + myId + ":" + LocalDate.now();
		
		// Set의 크기(size)가 곧 대화한 사람 수
		Long count = redisTemplate.opsForSet().size(key);
		
		return (count != null) ? count : 0L;
	}
}