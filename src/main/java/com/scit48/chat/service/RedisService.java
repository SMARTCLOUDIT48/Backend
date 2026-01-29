package com.scit48.chat.service;

import com.scit48.common.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedisService {
	
	// 1. 기존: 문자열/숫자 처리용 (인원수, 활동량)
	private final RedisTemplate<String, String> redisTemplate;
	
	// ✅ 2. 추가: 객체(JSON) 처리용 (채팅 내역 저장)
	// RedisConfig에서 @Bean 이름을 "redisObjectTemplate"으로 했으므로 변수명을 맞춥니다.
	@Qualifier("redisObjectTemplate")
	private final RedisTemplate<String, Object> redisObjectTemplate;
	
	// ==========================================
	// [기존 기능] 방 인원수 관리 (Counter)
	// ==========================================
	
	public void userEnter(String roomId) {
		ValueOperations<String, String> ops = redisTemplate.opsForValue();
		ops.increment("roomCount:" + roomId);
	}
	
	public void userLeave(String roomId) {
		ValueOperations<String, String> ops = redisTemplate.opsForValue();
		ops.decrement("roomCount:" + roomId);
	}
	
	public long getUserCount(String roomId) {
		ValueOperations<String, String> ops = redisTemplate.opsForValue();
		String count = ops.get("roomCount:" + roomId);
		return (count != null) ? Long.parseLong(count) : 0;
	}
	
	// ==========================================
	// [기존 기능] 오늘 활동량 집계
	// ==========================================
	
	public void recordInteraction(Long myId, Long partnerId) {
		String key = "daily:interaction:" + myId + ":" + LocalDate.now();
		redisTemplate.opsForSet().add(key, String.valueOf(partnerId));
		redisTemplate.expire(key, 1, TimeUnit.DAYS);
	}
	
	public Long getTodayInteractionCount(Long myId) {
		String key = "daily:interaction:" + myId + ":" + LocalDate.now();
		Long count = redisTemplate.opsForSet().size(key);
		return (count != null) ? count : 0L;
	}
	
	// ==========================================
	// ✅ [신규 기능] 채팅 내역 캐싱 (JSON 저장)
	// ==========================================
	
	/**
	 * 채팅 메시지를 Redis 리스트에 저장
	 * Key: chat:room:{roomId}:msg
	 */
	public void saveMessageToRedis(ChatMessageDto message) {
		String key = "chat:room:" + message.getRoomId() + ":msg";
		
		// redisObjectTemplate을 사용하여 객체(Dto)를 그대로 저장 (내부적으로 JSON 변환됨)
		redisObjectTemplate.opsForList().rightPush(key, message);
		
		// (선택) 메모리 관리를 위해 최근 200개 메시지만 유지하고 나머지는 자름
		// redisObjectTemplate.opsForList().trim(key, -200, -1);
		
		// 데이터 유효기간 설정 (예: 3일)
		redisObjectTemplate.expire(key, 3, TimeUnit.DAYS);
	}
	
	/**
	 * 채팅방 입장 시 지난 대화 내역 불러오기
	 */
	public List<ChatMessageDto> getChatHistory(String roomId) {
		String key = "chat:room:" + roomId + ":msg";
		
		// 리스트의 처음(0)부터 끝(-1)까지 가져옴
		List<Object> rawList = redisObjectTemplate.opsForList().range(key, 0, -1);
		
		if (rawList == null) {
			return List.of();
		}
		
		// Object -> ChatMessageDto 캐스팅
		return rawList.stream()
				.map(obj -> (ChatMessageDto) obj)
				.collect(Collectors.toList());
	}
}