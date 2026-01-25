package com.scit48.auth.controller;

import com.scit48.auth.dto.LoginRequestDto;
import com.scit48.auth.jwt.JwtToken;
import com.scit48.auth.service.AuthService;
import com.scit48.auth.repository.RefreshTokenRepository;
import com.scit48.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

        private final AuthService authService;
        private final RefreshTokenRepository refreshTokenRepository;

        /*
         * =========================
         * 로그인
         * =========================
         * POST /api/login
         */
        @PostMapping("/login")
        public ResponseEntity<ApiResponse<JwtToken>> login(
                        @RequestBody LoginRequestDto request) {

                JwtToken token = authService.login(request);
                return ResponseEntity.ok(ApiResponse.success(token));
        }

        /*
         * =========================
         * 토큰 재발급
         * =========================
         * POST /api/reissue
         */
        @PostMapping("/reissue")
        public ResponseEntity<ApiResponse<JwtToken>> reissue(
                        @RequestHeader("Authorization") String authorization) {

                String refreshToken = authorization.startsWith("Bearer ")
                                ? authorization.substring(7)
                                : authorization;

                JwtToken token = authService.reissue(refreshToken);
                return ResponseEntity.ok(ApiResponse.success(token));
        }

        @PostMapping("/logout")
        public ResponseEntity<ApiResponse<Void>> logout(
                        @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {

                long memberId = Long.parseLong(user.getUsername());

                refreshTokenRepository.delete(memberId);

                return ResponseEntity.ok(
                                ApiResponse.success(null, "로그아웃 완료"));
        }

}
