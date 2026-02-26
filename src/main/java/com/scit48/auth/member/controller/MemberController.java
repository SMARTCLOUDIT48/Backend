package com.scit48.auth.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scit48.auth.dto.SignupRequestDto;
import com.scit48.auth.jwt.JwtToken;
import com.scit48.auth.member.service.CustomUserDetails;
import com.scit48.auth.member.service.MemberInterestService;
import com.scit48.auth.service.AuthService;
import com.scit48.common.dto.UserDTO;
import com.scit48.common.dto.UserInterestDTO;
import com.scit48.common.exception.UnauthorizedException;
import com.scit48.common.response.ApiResponse;
import com.scit48.community.service.BoardService;
import com.scit48.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final MemberInterestService memberInterestService;
    private final BoardService boardService;

    /*
     * =========================
     * 회원가입 + 자동 로그인
     * =========================
     * POST /api/members
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<Void>> signup(
            @RequestPart("data") String data,
            @RequestPart(value = "image", required = false) MultipartFile image) throws Exception {

        SignupRequestDto request = objectMapper.readValue(data, SignupRequestDto.class);

        // 회원가입 + 토큰 발급
        JwtToken token = authService.signupAndLogin(request, image);

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", token.accessToken())
                .httpOnly(true)
                .path("/")
                .sameSite("Lax")
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
                .body(ApiResponse.success(null, "회원가입 및 로그인 완료"));
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
                ApiResponse.success(Map.of("available", !exists)));
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

    /*
     * =========================
     * 프로필 이미지 수정
     * =========================
     * PUT /api/members/me/profile-image
     */
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

    /*
     * =========================
     * 프로필 수정
     * =========================
     * PUT /api/members/me/profile
     */
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

    @PostMapping("/me/interests")
    public ApiResponse<Void> saveInterests(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody List<UserInterestDTO> interests) {

        if (userDetails == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        memberInterestService.saveUserInterests(
                userDetails.getUser().getId(),
                interests);

        return ApiResponse.success(null, "관심사 저장 완료");
    }

    @GetMapping("/me/interests")
    public ApiResponse<List<UserInterestDTO>> getMyInterests(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        List<UserInterestDTO> interests = memberInterestService.getUserInterests(
                userDetails.getUser().getId());

        return ApiResponse.success(interests);
    }

    /*
     * =========================
     * 게시글 개수
     * =========================
     */

    @GetMapping("/me/post-count")
    public ApiResponse<Long> getMyPostCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        String memberId = userDetails.getUser().getMemberId();
        long count = boardService.getMyPostCount(memberId);

        return ApiResponse.success(count);
    }

}
