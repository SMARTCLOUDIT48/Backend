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

    private final ProfileViewService service;

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