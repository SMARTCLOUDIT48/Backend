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
	 * 현재 로그인한 유저가 특정 유저에게 누른 반응 상태 조회
	 * 반환값 예시: "LIKE", "DISLIKE", 또는 null(안 누름)
	 */
	@GetMapping("/status")
	public ApiResponse<String> getReactionStatus(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam Long toUserId) {
		
		// 비로그인 상태면 아무 반응도 없는 것(null)으로 처리
		if (userDetails == null) {
			return ApiResponse.success(null);
		}
		
		Long fromUserId = userDetails.getUser().getId();
		
		// 💡 UserReactionService에 getReactionStatus 같은 메서드가 필요합니다!
		// (DB에서 두 사람 사이의 반응 내역을 찾아 "LIKE"나 "DISLIKE" 문자열로 반환하도록 백엔드에 구현해주세요)
		String status = userReactionService.getReactionStatus(fromUserId, toUserId);
		
		return ApiResponse.success(status);
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
