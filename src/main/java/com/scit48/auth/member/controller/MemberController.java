package com.scit48.auth.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scit48.auth.dto.SignupRequestDto;
import com.scit48.auth.member.service.CustomUserDetails;
import com.scit48.auth.service.AuthService;
import com.scit48.common.domain.entity.UserEntity;
import com.scit48.common.dto.UserDTO;
import com.scit48.common.exception.UnauthorizedException;
import com.scit48.common.response.ApiResponse;
import com.scit48.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import com.scit48.common.exception.BadRequestException;
import com.scit48.auth.member.controller.MemberController;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final AuthService authService;
    private final UserRepository userRepository;
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
            exists = userRepository.existsByMemberId(memberId);
        } else if (nickname != null && !nickname.isBlank()) {
            exists = userRepository.existsByNickname(nickname);
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
    public ApiResponse<UserDTO> me(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        return ApiResponse.success(
                UserDTO.fromEntity(userDetails.getUser()));
    }

    @PutMapping(value = "/me/profile-image", consumes = "multipart/form-data")
    public ApiResponse<Void> updateProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("image") MultipartFile image) {
        if (userDetails == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        authService.updateProfileImage(
                userDetails.getUser().getId(),
                image);

        return ApiResponse.success(null, "프로필 이미지 변경 완료");
    }

    @PutMapping("/me/profile")
    public ApiResponse<Void> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String intro,
            @RequestParam(required = false) String levelLanguage) {
        if (userDetails == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        authService.updateProfile(
                userDetails.getUser().getId(),
                intro,
                levelLanguage);

        return ApiResponse.success(null, "프로필 수정 완료");
    }

}
