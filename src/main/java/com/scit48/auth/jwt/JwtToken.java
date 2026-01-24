package com.scit48.auth.jwt;

/**
 * Access / Refresh Token 묶음 DTO
 * - 로그인, 재발급 응답용
 */
public record JwtToken(
                String accessToken,
                String refreshToken) {
}
