package com.scit48.common.controller;

import com.scit48.auth.member.service.CustomUserDetails;
import com.scit48.common.dto.UserDTO;
import com.scit48.common.enums.ReactionType;
import com.scit48.common.exception.UnauthorizedException;
import com.scit48.common.response.ApiResponse;
import com.scit48.common.service.UserReactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reactions")
public class UserReactionController {

    private final UserReactionService userReactionService;

    /**
     * =========================
     * 좋아요 / 싫어요
     * =========================
     * POST /api/reactions
     */
    @PostMapping
    public ApiResponse<Void> react(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long toUserId,
            @RequestParam ReactionType reaction) {

        if (userDetails == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        Long fromUserId = userDetails.getUser().getId();

        userReactionService.react(fromUserId, toUserId, reaction);
        return ApiResponse.success(null, "반응 처리 완료");
    }

    /**
     * =========================
     * 나를 좋아요 누른 사람 목록
     * =========================
     * GET /api/reactions/liked-me
     */
    @GetMapping("/liked-me")
    public ApiResponse<List<UserDTO>> likedMe(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        Long myUserId = userDetails.getUser().getId();

        return ApiResponse.success(
                userReactionService.getUsersWhoLikedMe(myUserId));
    }
}
