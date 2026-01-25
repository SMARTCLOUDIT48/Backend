package com.scit48.auth.repository;

import com.scit48.common.exception.UnauthorizedException;
import com.scit48.common.key.RedisKeyFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class RefreshTokenRepository {

    private static final long REFRESH_EXPIRE_DAYS = 14;
    private final RedisTemplate<String, String> redisTemplate;

    public RefreshTokenRepository(
            @Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(long memberId, String refreshToken) {
        redisTemplate.opsForValue().set(
                RedisKeyFactory.refreshToken(memberId),
                refreshToken,
                REFRESH_EXPIRE_DAYS,
                TimeUnit.DAYS);
    }

    public void validate(long memberId, String refreshToken) {
        String saved = redisTemplate.opsForValue()
                .get(RedisKeyFactory.refreshToken(memberId));

        if (saved == null || !saved.equals(refreshToken)) {
            throw new UnauthorizedException("Refresh Token이 유효하지 않습니다.");
        }
    }

    public void delete(long memberId) {
        redisTemplate.delete(RedisKeyFactory.refreshToken(memberId));
    }
}
