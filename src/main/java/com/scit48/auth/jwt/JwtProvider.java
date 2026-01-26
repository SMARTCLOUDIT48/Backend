package com.scit48.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT 생성 및 파싱 담당 Provider
 * - 토큰 생성
 * - 토큰 검증
 * - memberId 추출
 */
@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtKeyProvider keyProvider;

    // 토큰 만료 시간
    private static final long ACCESS_EXPIRE = 30 * 60 * 1000L; // 30분
    private static final long REFRESH_EXPIRE = 14 * 24 * 60 * 60 * 1000L; // 14일

    // Access Token 생성
    public String createAccessToken(Long memberId, String role) {
        Date now = new Date();

        return Jwts.builder()
                .setSubject(memberId.toString()) // 인증 주체
                .claim("role", role) // 권한
                .claim("type", "ACCESS") // 토큰 타입
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_EXPIRE))
                .signWith(keyProvider.getKey())
                .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken(Long memberId) {
        Date now = new Date();

        return Jwts.builder()
                .setSubject(memberId.toString())
                .claim("type", "REFRESH")
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + REFRESH_EXPIRE))
                .signWith(keyProvider.getKey())
                .compact();
    }

    // 토큰 파싱 (서명 + 만료 검증 포함)
    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(keyProvider.getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 토큰 유효성 검사
    public boolean validate(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 토큰에서 memberId 추출
    public Long getMemberId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }
}
