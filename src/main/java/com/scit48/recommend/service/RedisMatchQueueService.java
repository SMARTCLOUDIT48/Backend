package com.scit48.recommend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
@RequiredArgsConstructor
public class RedisMatchQueueService {
	
	private final RedisTemplate<String, String> redisTemplate;
	
	private static final String QUEUE_KEY = "match:queue:global";
	private static final String WAITING_SET_KEY = "match:waiting:global";
	private static final String CRITERIA_PREFIX = "match:criteria:";
	
	private static final long CRITERIA_TTL_MIN = 30;
	
	// 큐에서 1명 꺼내고(waiting에서도 제거) partnerId 반환, 없으면 0
	private static final String LUA_POP_ONE = """
        local queueKey = KEYS[1]
        local waitingKey = KEYS[2]
        local partner = redis.call('LPOP', queueKey)
        if not partner then
          return 0
        end
        redis.call('SREM', waitingKey, partner)
        return tonumber(partner)
    """;
	
	public void saveCriteria(Long userId, String criteriaKey) {
		if (criteriaKey == null) return;
		redisTemplate.opsForValue().set(CRITERIA_PREFIX + userId, criteriaKey, CRITERIA_TTL_MIN, TimeUnit.MINUTES);
	}
	
	public String getCriteria(Long userId) {
		return redisTemplate.opsForValue().get(CRITERIA_PREFIX + userId);
	}
	
	public void clearCriteria(Long userId) {
		redisTemplate.delete(CRITERIA_PREFIX + userId);
	}
	
	public boolean isWaiting(Long userId) {
		Boolean member = redisTemplate.opsForSet().isMember(WAITING_SET_KEY, String.valueOf(userId));
		return member != null && member;
	}
	
	// 대기 등록(중복 방지)
	public void enqueueIfAbsent(Long userId) {
		String uid = String.valueOf(userId);
		Long added = redisTemplate.opsForSet().add(WAITING_SET_KEY, uid);
		if (added != null && added == 1L) {
			redisTemplate.opsForList().rightPush(QUEUE_KEY, uid);
		}
	}
	
	// partner 1명 pop (없으면 null)
	public Long popPartner() {
		DefaultRedisScript<Long> script = new DefaultRedisScript<>();
		script.setScriptText(LUA_POP_ONE);
		script.setResultType(Long.class);
		
		Long result = redisTemplate.execute(script, List.of(QUEUE_KEY, WAITING_SET_KEY));
		if (result == null || result == 0L) return null;
		return result;
	}
}

