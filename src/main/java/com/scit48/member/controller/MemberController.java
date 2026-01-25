package com.scit48.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scit48.auth.dto.SignupRequestDto;
import com.scit48.auth.service.AuthService;
import com.scit48.common.domain.entity.UserEntity;
import com.scit48.common.response.ApiResponse;
import com.scit48.common.repository.UserRepository;
import com.scit48.member.dto.MyPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final AuthService authService;
    private final UserRepository memberRepository;
    private final ObjectMapper objectMapper;

    /*
     * =========================
     * 회원가입 (개발단계용: 결과 보이게)
     * =========================
     * POST /api/members
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> signup(
            @RequestPart("data") String data,
            @RequestPart(value = "image", required = false) MultipartFile image)
            throws Exception {

        SignupRequestDto request = objectMapper.readValue(data, SignupRequestDto.class);

        UserEntity saved = authService.signup(request, image);

        Map<String, Object> responseData = Map.of(
                "id", saved.getId(),
                "memberId", saved.getMemberId(),
                "nickname", saved.getNickname(),
                "gender", saved.getGender(),
                "age", saved.getAge(),
                "nation", saved.getNation(),
                "nativeLanguage", saved.getNativeLanguage(),
                "levelLanguage", saved.getLevelLanguage(),
                "role", saved.getRole(),
                "createdAt", saved.getCreatedAt());

        return ResponseEntity.ok(
                ApiResponse.success(responseData, "회원가입 완료"));
    }

    /*
     * =========================
     * 아이디 / 닉네임 중복 확인
     * =========================
     * GET /api/members/exists
     */
    @GetMapping("/exists")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> exists(
            @RequestParam(required = false) String memberId,
            @RequestParam(required = false) String nickname) {

        boolean exists;

        if (memberId != null && !memberId.isBlank()) {
            exists = memberRepository.existsByMemberId(memberId);
        } else if (nickname != null && !nickname.isBlank()) {
            exists = memberRepository.existsByNickname(nickname);
        } else {
            throw new IllegalArgumentException("memberId 또는 nickname 중 하나는 필요합니다.");
        }

        return ResponseEntity.ok(
                ApiResponse.success(
                        Map.of("available", !exists)));
    }

    /*
     * =========================
     * 마이페이지
     * =========================
     * GET /api/members/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyPageResponse>> myPage(
            @AuthenticationPrincipal User user) {

        long userId = Long.parseLong(user.getUsername());

        UserEntity member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        MyPageResponse response = new MyPageResponse(
                member.getMemberId(),
                member.getNickname(),
                member.getGender(),
                member.getAge(),
                member.getNation(),
                member.getManner().doubleValue(),
                member.getIntro(),
                member.getProfileImagePath());

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
