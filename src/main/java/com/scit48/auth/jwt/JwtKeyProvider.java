package com.scit48.auth.jwt;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

/**
 * JWT 서명에 사용할 Secret Key 제공 클래스
 * - 애플리케이션 시작 시 한 번만 생성
 */
@Component
public class JwtKeyProvider {

    private final Key key;

    public JwtKeyProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public Key getKey() {
        return key;
    }
}
