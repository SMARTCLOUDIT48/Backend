package com.scit48.auth.controller;

import com.scit48.auth.dto.LoginRequestDto;
import com.scit48.auth.dto.SignupRequestDto;
import com.scit48.auth.jwt.JwtToken;
import com.scit48.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입 (multipart/form-data)
     */
    @PostMapping(value = "/signup", consumes = "multipart/form-data")
    public ResponseEntity<Void> signup(
            @RequestPart("data") SignupRequestDto request,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        authService.signup(request, image);
        return ResponseEntity.ok().build();
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<JwtToken> login(@RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Access Token 재발급
     */
    @PostMapping("/reissue")
    public ResponseEntity<JwtToken> reissue(
            @RequestHeader("Authorization") String authorization) {

        String refreshToken = authorization.startsWith("Bearer ")
                ? authorization.substring(7)
                : authorization;

        return ResponseEntity.ok(authService.reissue(refreshToken));
    }
}
