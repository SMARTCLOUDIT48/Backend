package com.scit48.auth.controller;

import com.scit48.auth.dto.LoginRequestDto;
import com.scit48.auth.jwt.JwtToken;
import com.scit48.auth.repository.RefreshTokenRepository;
import com.scit48.auth.service.AuthService;
import com.scit48.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

        private final AuthService authService;
        private final RefreshTokenRepository refreshTokenRepository;

        // =========================
        // 로그인
        // =========================
        @PostMapping("/login")
        public ResponseEntity<ApiResponse<Void>> login(
                        @RequestBody LoginRequestDto request) {

                JwtToken token = authService.login(request);

                ResponseCookie accessCookie = ResponseCookie.from("accessToken", token.accessToken())
                                .httpOnly(true)
                                .path("/")
                                .sameSite("Lax") // 프론트/백 분리면 None
                                .maxAge(30 * 60)
                                .build();

                ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", token.refreshToken())
                                .httpOnly(true)
                                .path("/")
                                .sameSite("Lax")
                                .maxAge(14 * 24 * 60 * 60)
                                .build();

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                                .body(ApiResponse.success(null, "로그인 완료"));
        }

        // =========================
        // 로그아웃 (쿠키 기준)
        // =========================
        @PostMapping("/logout")
        public ResponseEntity<ApiResponse<Void>> logout(
                        @CookieValue(value = "refreshToken", required = false) String refreshToken,
                        HttpServletResponse response) {

                if (refreshToken != null) {
                        refreshTokenRepository.deleteByToken(refreshToken);
                }

                ResponseCookie deleteAccess = ResponseCookie.from("accessToken", "")
                                .httpOnly(true)
                                .path("/")
                                .maxAge(0)
                                .build();

                ResponseCookie deleteRefresh = ResponseCookie.from("refreshToken", "")
                                .httpOnly(true)
                                .path("/")
                                .maxAge(0)
                                .build();

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, deleteAccess.toString())
                                .header(HttpHeaders.SET_COOKIE, deleteRefresh.toString())
                                .body(ApiResponse.success(null, "로그아웃 완료"));
        }

        // =========================
        // 토큰 재발급
        // =========================
        @PostMapping("/reissue")
        public ResponseEntity<ApiResponse<Void>> reissue(
                        @CookieValue(value = "refreshToken", required = false) String refreshToken) {

                if (refreshToken == null) {
                        return ResponseEntity.status(401)
                                        .body(ApiResponse.error("리프레시 토큰 없음"));
                }

                JwtToken token = authService.reissue(refreshToken);

                ResponseCookie accessCookie = ResponseCookie.from("accessToken", token.accessToken())
                                .httpOnly(true)
                                .path("/")
                                .sameSite("Lax")
                                .maxAge(30 * 60)
                                .build();

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                                .body(ApiResponse.success(null, "재발급 완료"));
        }
}
