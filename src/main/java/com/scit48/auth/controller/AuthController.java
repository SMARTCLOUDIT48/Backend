package com.scit48.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping(value = "/signup", consumes = "multipart/form-data")
    public ResponseEntity<Void> signup(
            @RequestPart("data") String data,
            @RequestPart(value = "image", required = false) MultipartFile image) throws Exception {

        System.out.println("===== SIGNUP RAW DATA =====");
        System.out.println(data);

        SignupRequestDto request = objectMapper.readValue(data, SignupRequestDto.class);

        System.out.println("===== SIGNUP PARSED DTO =====");
        System.out.println("memberId = " + request.getMemberId());
        System.out.println("password = " + request.getPassword());
        System.out.println("nickname = " + request.getNickname());
        System.out.println("gender = " + request.getGender());
        System.out.println("age = " + request.getAge());
        System.out.println("nation = " + request.getNation());
        System.out.println("nativeLanguage = " + request.getNativeLanguage());
        System.out.println("levelLanguage = " + request.getLevelLanguage());

        authService.signup(request, image);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<JwtToken> login(@RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/reissue")
    public ResponseEntity<JwtToken> reissue(
            @RequestHeader("Authorization") String authorization) {

        String refreshToken = authorization.startsWith("Bearer ")
                ? authorization.substring(7)
                : authorization;

        return ResponseEntity.ok(authService.reissue(refreshToken));
    }
}
