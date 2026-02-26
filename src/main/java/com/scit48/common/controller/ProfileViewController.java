package com.scit48.common.controller;

import com.scit48.auth.member.service.CustomUserDetails;
import com.scit48.common.dto.UserDTO;
import com.scit48.common.exception.UnauthorizedException;
import com.scit48.common.response.ApiResponse;
import com.scit48.common.service.ProfileViewService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile-views")
public class ProfileViewController {

    /**
     * ==========================================
     * 프로필 조회 기록 API
     * ==========================================
     * POST /api/profile-views/{targetId}
     *
     * [기능]
     * - 로그인한 사용자가 다른 사용자의 프로필을 조회했을 때
     * 조회 기록을 저장한다.
     *
     * [동작 방식]
     * 1. 인증된 사용자 정보(@AuthenticationPrincipal) 확인
     * 2. 로그인하지 않은 경우 아무 동작도 하지 않음
     * 3. 로그인한 경우,
     * service.recordView(조회자ID, 대상자ID)를 호출하여
     * 프로필 조회 기록을 저장한다.
     *
     * @param user     로그인 사용자 정보
     * @param targetId 조회 대상 사용자 ID
     */
    private final ProfileViewService service;

	// userPage 들어갈 때
    @PostMapping("/{targetId}")
    public void record(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long targetId) {

        if (user == null)
            return;

        service.recordView(user.getUser().getId(), targetId);
    }

    @GetMapping("/me")
    public ApiResponse<List<UserDTO>> myVisitors(
            @AuthenticationPrincipal CustomUserDetails user) {

        if (user == null) {
            throw new UnauthorizedException("로그인 필요");
        }

        return ApiResponse.success(
                service.getRecentVisitors(user.getUser().getId()));
    }
}