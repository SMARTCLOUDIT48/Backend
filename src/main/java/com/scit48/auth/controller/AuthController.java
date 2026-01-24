package com.scit48.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scit48.auth.dto.LoginRequestDto;
import com.scit48.auth.dto.SignupRequestDto;
import com.scit48.auth.jwt.JwtToken;
import com.scit48.auth.service.AuthService;
import com.scit48.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

        private final AuthService authService;
        private final MemberRepository memberRepository;

        private final ObjectMapper objectMapper = new ObjectMapper();

        /*
         * =========================
         * 회원가입
         * =========================
         */
        @PostMapping(value = "/signup", consumes = "multipart/form-data")
        public ResponseEntity<Void> signup(
                        @RequestPart("data") String data,
                        @RequestPart(value = "image", required = false) MultipartFile image) throws Exception {

                SignupRequestDto request = objectMapper.readValue(data, SignupRequestDto.class);

                authService.signup(request, image);
                return ResponseEntity.ok().build();
        }

        /*
         * =========================
         * 로그인
         * =========================
         */
        @PostMapping("/login")
        public ResponseEntity<JwtToken> login(
                        @RequestBody LoginRequestDto request) {
                return ResponseEntity.ok(authService.login(request));
        }

        /*
         * =========================
         * 토큰 재발급
         * =========================
         */
        @PostMapping("/reissue")
        public ResponseEntity<JwtToken> reissue(
                        @RequestHeader("Authorization") String authorization) {
                String refreshToken = authorization.startsWith("Bearer ")
                                ? authorization.substring(7)
                                : authorization;

                return ResponseEntity.ok(authService.reissue(refreshToken));
        }

        /*
         * =========================
         * 아이디 중복 확인
         * =========================
         */
        @GetMapping("/check-member-id")
        public Map<String, Boolean> checkMemberId(
                        @RequestParam String memberId) {
                boolean exists = memberRepository.findByMemberId(memberId).isPresent();

                return Map.of("available", !exists);
        }
}
