package com.scit48.recommend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class RedisMatchQueueService {
	private final RedisTemplate<String, String> redisTemplate;
	
	private static final String LUA_TRY_MATCH = """
        local queueKey = KEYS[1]
        local waitingSetKey = KEYS[2]
        local me = ARGV[1]

        -- 이미 대기 중이면(중복 클릭 방지)
        if redis.call('SISMEMBER', waitingSetKey, me) == 1 then
            return 0
        end

        -- 상대 1명 꺼내기
        local partner = redis.call('LPOP', queueKey)
        if not partner then
            -- 대기열 비었으면 내가 들어감
            redis.call('RPUSH', queueKey, me)
            redis.call('SADD', waitingSetKey, me)
            return 0
        end

        -- 혹시 내가 뽑히는 이상 케이스 방어
        if partner == me then
            redis.call('RPUSH', queueKey, me)
            redis.call('SADD', waitingSetKey, me)
            return 0
        end

        -- 상대는 이제 대기 상태에서 제거
        redis.call('SREM', waitingSetKey, partner)
        return tonumber(partner)
    """;
	
	/**
	 * @return partnerId (매칭 성공) / null (대기)
	 */
	public Long tryMatch(String criteriaKey, Long myId) {
		String queueKey = "match:queue:" + criteriaKey;
		String waitingSetKey = "match:waiting:" + criteriaKey;
		
		DefaultRedisScript<Long> script = new DefaultRedisScript<>();
		script.setScriptText(LUA_TRY_MATCH);
		script.setResultType(Long.class);
		
		Long result = redisTemplate.execute(
				script,
				List.of(queueKey, waitingSetKey),
				String.valueOf(myId)
		);
		
		if (result == null || result == 0) return null;
		return result;
	}
}
