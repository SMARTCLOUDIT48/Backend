package com.scit48.auth.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository

public class RefreshTokenRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public RefreshTokenRepository(
            @Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final long REFRESH_EXPIRE_DAYS = 14;

    private String key(long memberId) {
        return "refresh:" + memberId;
    }

    public void save(long memberId, String refreshToken) {
        redisTemplate.opsForValue().set(
                key(memberId),
                refreshToken,
                REFRESH_EXPIRE_DAYS,
                TimeUnit.DAYS);
    }

    public void validate(long memberId, String refreshToken) {
        String saved = redisTemplate.opsForValue().get(key(memberId));
        if (saved == null || !saved.equals(refreshToken)) {
            throw new IllegalArgumentException("Refresh Token이 유효하지 않습니다.");
        }
    }

    public void delete(long memberId) {
        redisTemplate.delete(key(memberId));
    }
}